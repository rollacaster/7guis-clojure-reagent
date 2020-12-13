(ns tech.thomas-sojka.sevenguis.cells
  (:require [cljs.pprint :as pp]
            [cljs.reader :as edn]
            [clojure.string :as str]
            [reagent.core :as r]))

(defonce cells-state (r/atom {:cells (into [] (for [_ (range 100)] (into [](for [_ (range 26)] {:value "" }))))
                              :selected nil}))

(defn params-coords [formula f-row f-col]
  (->> (edn/read-string (last (re-matches #"^\=.*(\(.*\))" formula)))
       (map
        (fn [coord]
          (let [[letter number] (drop 1 (re-matches #"^([a-zA-Z])(\d+)$" (str coord)))
                row (js/parseInt number)
                col (- (pp/char-code (str/upper-case letter)) 65)]
            [row col])))
       (remove #(= [f-row f-col] %))))

(defn get-formula-args [cells param-coords]
  (map
   (fn [[row col]]
     (let [value (js/parseInt (:value (nth (nth cells row) col)))]
       (if (js/isNaN value) 0 value)))
   param-coords))

(defn apply-formula [cells row col formula params]
  (assoc-in cells [row col :value]
            (let [args (get-formula-args cells params)]
              (apply
               (let [fn-name (last (re-find #"^\=(.*)\(" formula))]
                 (case fn-name
                   "SUM" +
                   "PRODUCT" *))
               args))))

(defn rerun-formula-params [cells params]
  (reduce (fn [cells [p-row p-col]]
            (let [{:keys [formula]} (nth (nth cells p-row) p-col)]
              (if formula
                (apply-formula cells p-row p-col formula (params-coords formula p-row p-col))
                cells)))
          cells
          params))

(defn run-notify [cells notify]
  (reduce
   (fn [cells [n-row n-col]]
     (let [{:keys [formula]} ((nth cells n-row) n-col)
           params (params-coords formula n-row n-col) ]
       (-> cells
           (rerun-formula-params params)
           (apply-formula n-row n-col formula params))))
   cells
   notify))

(defn add-notify [cells params row col]
  (reduce
   (fn [cells [c-row c-col]]
     (update-in cells [c-row c-col :notify] conj [row col]))
   cells
   params))

(defn add-formula [cells row col formula]
  (let [params (params-coords formula row col)]
    (-> cells
        (apply-formula row col formula params)
        (assoc-in [row col :formula] formula)
        (add-notify params row col))))

(defn update-value [cells row col value]
  (if (str/starts-with? value "=")
    (add-formula cells row col value)
    (update-in cells [row col] assoc :value value)))

(defn clear-notify [cells row col]
  (let [{:keys [formula]} (nth (nth cells row) col)]
    (if formula
      (update-in
       (let [params (params-coords formula row col)]
         (reduce
          (fn [cells [p-row p-col]]
            (update-in cells [p-row p-col] update :notify (fn [notify] (remove #(= % [row col]) notify))))
          cells
          params))
       [row col]
       dissoc
       :formula)
      cells)))

(defn update-cell [cells row col value]
  (let [{:keys [notify]} (nth (nth cells row) col)]
    (-> cells
        (clear-notify row col)
        (update-value row col value)
        (run-notify notify))))

(comment
  (-> (into [] (for [_ (range 20)] (into [](for [_ (range 5)] {:value "" }))))
      (update-cell 0 0 "5")
      (update-cell 0 1 "5")
      (update-cell 0 2 "=SUM(A0 B0 C0)))")
      (update-cell 0 3 "=PRODUCT(A0 B0 C0)))")
      (update-cell 0 1 "10"))
  )

(defn cells []
  [:<>
   (let [{:keys [selected cells]} @cells-state]
     [:div.overflow-scroll
      {:style {:height "23rem" :width "28rem"}}
      [:div.relative
       {:style {:width "131rem"}}
       [:div.flex.sticky.top-0.bg-gray-100
        [:span.w-8.h-4]
        (map
         (fn [char-idx] [:div.w-20.text-center {:key char-idx} (char char-idx)])
         (range  65 (+ 65 (count (first cells)))))]
       (doall
        (map-indexed
         (fn [row contents]
           [:div.flex {:key row :class "h-8"}
            [:div
             [:div.tabular-nums.pv-2.w-8.h-8 row]]
            (doall
             (map-indexed
              (fn [col]
                (let [ref (atom nil)
                      active (= selected [row col])]
                  [:div {:key col}
                   [:button
                    {:on-double-click #(do
                                         (swap! cells-state
                                                (fn [state]
                                                  (-> state
                                                      (assoc :selected [row col])
                                                      (update-in [:cells row col]
                                                                 (fn [cell]
                                                                   (if (:formula cell)
                                                                     (assoc cell :value (:formula cell))
                                                                     cell))))))
                                         ;; Focus hack no clue why this is necessary 🙈
                                         (let [ref @ref] (js/setTimeout (fn [] (.focus ref)) 1)))}
                    [:input.h-8.px-2.border.w-20
                     {:value (:value (nth (nth cells row) col))
                      :ref #(reset! ref %)
                      :disabled (not active)
                      :on-blur #(swap! cells-state
                                       (fn [state]
                                         (-> state
                                             (update :cells update-cell row col  ^js (.-target.value %))
                                             (assoc :selected nil))))
                      :on-change #(swap! cells-state assoc-in [:cells row col :value]  ^js (.-target.value %))}]]]))
              contents))])
         cells))]])
   [:div.py-2
    [:div.mb-2 "Double-Click to edit cell."]
    [:div "Formulas:"]
    [:ul.pl-3
     [:li
      [:pre
       "=SUM(A0 A1)"]]
     [:li
      [:pre
       "=PRODUCT(A0 A1)"]]]]])

