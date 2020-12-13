(ns tech.thomas-sojka.sevenguis.cells
  (:require [cljs.pprint :as pp]
            [cljs.reader :as edn]
            [clojure.string :as str]
            [reagent.core :as r]))

(defonce cells-state (r/atom {:cells (into [] (for [_ (range 20)] (into [](for [_ (range 5)] {:value "" }))))
                              :selected nil}))

(defn params-coords [formula]
  (map
   (fn [coord]
     (let [[letter number] (drop 1 (re-matches #"^([a-zA-Z])(\d+)$" (str coord)))
           row (js/parseInt number)
           col (- (pp/char-code (str/upper-case letter)) 65)]
       [row col]))
   (edn/read-string (last (str/split formula "=SUM")))))

(defn params-args [cells params-coords]
  (map
   (fn [[row col]]
     (let [value (js/parseInt (:value (nth (nth cells row) col)))]
       (if (js/isNaN value) 0 value)))
   params-coords))

(defn run-notify [cells notify]
  (reduce
   (fn [cells [n-row n-col]]
     (let [{:keys [formula]} ((nth cells n-row) n-col)
           params (params-coords formula)
           updated-cells (reduce (fn [cells [p-row p-col]]
                                   (let [{:keys [formula]} (nth (nth cells p-row) p-col)]
                                     (if formula
                                       (let [params (params-coords formula)
                                             args (params-args cells params)]
                                         (assoc-in cells [p-row p-col :value] (apply + args)))
                                       cells)))
                                 cells
                                 params)
           args (params-args updated-cells params)]
       (assoc-in updated-cells
                 [n-row n-col :value]
                 (apply + args))))
   cells
   notify))

(defn add-notify [cells params row col]
  (reduce
   (fn [cells [c-row c-col]]
     (update-in cells [c-row c-col :notify] conj [row col]))
   cells
   params))

(defn add-formula [cells row col formula params]
  (update-in cells [row col]
             (fn [cell]
               (-> cell
                   (assoc :formula formula)
                   (assoc :value (let [args (params-args cells params)] (apply + args)))))))

(defn clear-notify [cells row col]
  (let [{:keys [formula]} (nth (nth cells row) col)]
    (if formula
      (update-in
       (let [params (params-coords formula)]
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
    (if (str/starts-with? value "=SUM")
      (let [params (params-coords value)]
        (-> cells
            (add-formula row col value params)
            (add-notify params row col)
            (run-notify notify)))
      (-> cells
          (clear-notify row col)
          (update-in [row col] assoc :value value)
          (run-notify notify)))))

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
    [:div "Double-Click to edit cell."]
    [:div "Formulas:"]
    [:ul
     [:li
      [:pre
       "=SUM(A0 A1)"]]]]])

