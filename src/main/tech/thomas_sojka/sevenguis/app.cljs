(ns tech.thomas-sojka.sevenguis.app
  (:require [reagent.core :as r]
            [reagent.dom :as dom]
            [clojure.string :as str]))

(defn task-container [{:keys [title]} children]
  [:div.shadow-lg.mb-6
   [:header.bg-gray-200.text-center.rounded-t.border-t.border-l.border-r.border-gray-400
    title]
   [:main.bg-gray-100.p-2.rounded-b.border.border-gray-400
    children]])

(defn button [{:keys [on-click class disabled]} children]
  [:button.px-5.rounded.border
   {:on-click on-click
    :class (r/class-names class (if disabled
                                  "border-gray-400 text-gray-500"
                                  "bg-gray-200 border-gray-500"))
    :disabled disabled}
   children])

(defn counter []
  (let [count (r/atom 0)]
    (fn []
      [:div.flex
       [:div.text-center.px-10.w-5
        @count]
       [:div
        [button
         {:on-click #(swap! count inc)} "Count"]]])))

(defn input [{:keys [value on-change class disabled id error]}]
  [:input.mr-3.rounded.px-2.border
   {:value value :class (r/class-names class (when error "bg-red-200"))
    :on-change (fn [e] (on-change ^js (.-target.value e)))
    :disabled disabled :id id}])

(defn fahrenheit->celsius [temperature]
  (Math/round (* (- temperature 32) (/ 5 9))))

(defn celsius->fahrenheit [temperature]
  (Math/round (+ (* temperature (/ 9 5)) 32)))

(defonce temperatures
  (r/atom {:celsius nil
           :fahrenheit nil
           :error nil}))

(defn temperature-converter []
  [:div.flex
   [:div
    [input {:id "celsius"
            :value (:celsius @temperatures)
            :error (= (:error @temperatures) :celsius)
            :class (when (= (:error @temperatures) :fahrenheit) "bg-gray-500")
            :disabled (= (:error @temperatures) :fahrenheit)
            :on-change
            (fn [new-celsius]
              (swap!
               temperatures
               (fn [temperatures]
                 (let [updated-temperatures (assoc temperatures :celsius new-celsius)]
                   (if (re-find #"^\d*\.?\d+$" (str new-celsius))
                     (-> updated-temperatures
                         (assoc :fahrenheit (celsius->fahrenheit (js/parseFloat new-celsius)))
                         (assoc :error nil))
                     (assoc updated-temperatures :error :celsius))))))}]
    [:label {:for "celsius"} "Celsius"]]
   [:div.px-6 "="]
   [:div
    [input {:id "fahrenheit"
            :value (:fahrenheit @temperatures)
            :error (= (:error @temperatures) :fahrenheit)
            :class (when (= (:error @temperatures) :celsius) "bg-gray-500")
            :disabled (= (:error @temperatures) :celsius)
            :on-change
            (fn [new-fahrenheit]
              (swap!
               temperatures
               (fn [temperatures]
                 (let [updated-temperatures (assoc temperatures :fahrenheit new-fahrenheit)]
                   (if (re-find #"^\d*\.?\d+$" (str new-fahrenheit))
                     (-> updated-temperatures
                         (assoc :celsius (fahrenheit->celsius (js/parseFloat new-fahrenheit)))
                         (assoc :error nil))
                     (assoc updated-temperatures :error :fahrenheit))))))}]
    [:label {:for "fahrenheit"} "Fahrenheit"]]])

(defonce flights
  (r/atom
   {:type :one-way
    :start {:value "2020-12-04" :error false}
    :return {:value "2020-12-04" :error false}}))

(defn invalid-date [date]
  (js/isNaN (inst-ms (new js/Date date))))

(defn flight-booker []
  (let [{:keys [type start return]} @flights]
    [:div
     [:div.mb-3
      [:select.w-full.border.py-1.rounded
       {:value type
        :on-change #(swap! flights assoc :type (keyword ^js (.-target.value %)))}
       [:option {:value :one-way} "one-way-flight"]
       [:option {:value :return} "return-flight"]]]
     [:div.mb-3
      [input {:value (:value start) :class "w-full" :error (:error start)
              :on-change #(swap! flights assoc :start {:value % :error (invalid-date %)})}]]
     [:div.mb-3
      [input {:value (:value return) :error (:error return)
              :class (r/class-names "w-full" (when (= type :one-way) "bg-gray-200 text-gray-500"))
              :on-change #(swap! flights assoc :return {:value % :error (invalid-date %)})
              :disabled (= type :one-way)}]]
     [:div
      [button {:on-click #(js/alert (str "You have booked a " (when (= type :one-way) "one-way") " flight on " (:value start)
                                         (if (= type :one-way) "." (str " returning on " (:value return) "."))))
               :class "w-full"
               :disabled (or
                          (or (:error start) (and (:error return) (= type :return)))
                          (and (= type :return)
                               (< (.getTime (new js/Date (:value return))) (.getTime (new js/Date (:value start))))))}
       "Book"]]]))

(defonce duration-state (r/atom {:duration 0
                                 :time 0}))
;; TODO ClearInterval
(defonce interval (js/setInterval
                   #(swap! duration-state
                           (fn [{:keys [time duration] :as state}]
                             (assoc state :time (if (< time duration) (+ time 0.1) time))))
                   100))

(defn input-range [{:keys [value on-change min max]}]
  [:<>
   [:style "@media screen and (-webkit-min-device-pixel-ratio: 0) {
              input[type=\"range\"]::-webkit-slider-thumb {
                width: 15px;
                -webkit-appearance: none;
                appearance: none;
                height: 15px;
                cursor: ew-resize;
                background: #63b3ed;
                border-radius: 50%;
              }
            }"]
   [:input.w-full.appearance-none.bg-gray-400.h-3.rounded.border.border-gray-500
    {:type "range" :min min :max max :value value :on-change #(on-change ^js (.-target.value %))}]])

(defn timer []
  (let [{:keys [duration time]} @duration-state]
    [:div {:style {:width "20rem"}}
     [:div.flex.items-center.mb-4
      [:div.mr-3 {:class "w-1/3"} "Elapsed time:"]
      [:div.border.rounded.border-gray-500
       {:class "w-2/3"}
       [:div.h-4.bg-gray-400.rounded.w-full
        [:div.bg-blue-400.h-4.rounded-l
         {:style {:width (str (if (= duration 0) "100" (min (* (/ time duration) 100)
                                                            100)) "%")}}]]]]
     [:div.flex.mb-4
      [:div.mr-3 {:class "w-1/3"}]
      [:div (str (.toFixed time 1) "s")]]
     [:div.flex.items-center.mb-4
      [:div.mr-3
       {:class "w-1/3"}
       "Duration:"]
      [:div
       {:class "w-2/3"}
       [input-range {:value duration
               :on-change #(swap! duration-state assoc :duration (js/parseInt %))
               :min 0
               :max 30}]]]
     [button {:class "w-full" :on-click #(swap! duration-state assoc :time 0)} "Reset"]]))

(defonce crud-state
  (r/atom
   {:persons [{:name "Hans" :surname "Emil"}
              {:name "Mustermann" :surname "Max"}
              {:name "Tisch" :surname "Roman"}]
    :selected nil
    :name ""
    :filter-prefix ""
    :surname ""}))

(defn drop-index [col idx]
  (into []
        (filter identity (map-indexed #(when (not= %1 idx) %2) col))))

(defn crud []
  (let [{:keys [persons selected name surname filter-prefix]} @crud-state]
    [:div
     [:div.mb-4.flex
      {:style {:width "15rem"}}
      [:span.mr-3 {:class "w-1/2"} "Filter prefix:"]
      [input {:class "w-1/2" :value filter-prefix
              :on-change #(swap! crud-state assoc :filter-prefix %)}]]
     [:div.flex.mb-4.flex-wrap.md:flex-no-wrap
      [:ul.overflow-y-auto.h-32.bg-white.border.rounded.mr-3.mb-3.md:mb-0
       {:style {:width "15rem"}}
       (->> persons
            (filter (fn [{:keys [name surname]}]
                      (or (str/includes?
                           (str/lower-case name)
                           (str/lower-case filter-prefix))
                          (str/includes?
                           (str/lower-case surname)
                           (str/lower-case filter-prefix)))))
            (map-indexed
             (fn [idx {:keys [name surname]}]
               [:li {:key idx}
                [:button.pl-2.py-1.w-full.text-left
                 {:class (when (= idx selected) "bg-blue-200")
                  :on-click #(swap! crud-state assoc :selected idx)}
                 (str surname ", " name)]])))]
      [:div
       {:class "w-1/2"}
       [:div.flex.mb-3
        [:label {:class "w-1/2" :for "crud-name"} "Name:"]
        [input {:class "w-32" :id "crud-name"
                :value name
                :on-change #(swap! crud-state assoc :name %)}]]
       [:div.flex
        [:label {:class "w-1/2" :for "crud-surname"} "Surname:"]
        [input {:class "w-32" :id "crud-surname"
                :value surname
                :on-change #(swap! crud-state assoc :surname %)}]]]]
     [:div
      [button
       {:class "mr-3"
        :on-click #(when (and (not-empty name) (not-empty surname))
                     (swap! crud-state update :persons conj
                            {:name name :surname surname}))}
       "Create"]
      [button
       {:class "mr-3"
        :on-click #(when (and selected (not-empty name) (not-empty surname))
                     (swap! crud-state assoc-in [:persons selected]
                            {:name name :surname surname}))}
       "Update"]
      [button
       {:class "mr-3"
        :on-click #(when selected
                     (swap! crud-state update :persons drop-index selected))}
       "Delete"]]]))

(def circle-drawer-state
  (r/atom {:actions []
           :inactive-actions 0
           :context-menu nil}))


(defn circle-drawer-reducer [actions]
  (reduce
   (fn [state {:keys [type] :as action}]
     (case type
       :create (conj state {:x (:x action) :y (:y action) :r (:r action)})
       :radius (map
                (fn [{:keys [x y] :as circle}]
                  (if (and (= x (:x action))
                           (= y (:y action)))
                    (assoc circle :r (:r action))
                    circle))
                state)))
   []
   (reverse actions)))

(defn add-action [action]
  (swap! circle-drawer-state
         (fn [{:keys [actions inactive-actions] :as state}]
           (-> state
               (assoc :actions (conj (drop inactive-actions actions) action))
               (assoc :inactive-actions 0)
               (assoc :context-menu nil)))))

(defn circles [{:keys [actions inactive-actions]}]
  (circle-drawer-reducer (drop inactive-actions actions)))

(defn in-circle? [{:keys [x y r]} [test-x test-y]]
  (and (< (- x r) test-x) (> (+ x r) test-x)
       (< (- y r) test-y) (> (+ y r) test-y)))

(def circle-drawer
  (let [container (atom nil)
        outside-click-handler (fn []
                                (let [{:keys [context-menu]} @circle-drawer-state]
                                  (case (:type context-menu)
                                    :show
                                    (swap! circle-drawer-state assoc :context-menu nil)
                                    :update-circle
                                    (let [{:keys [x y r]} context-menu]
                                      (add-action {:type :radius :x x :y y :r r}))
                                    nil)))
        context-menu-handler #(do
                               (.preventDefault %)
                               (let [parentRect (.getBoundingClientRect @container)
                                     context-x (- (.-clientX %) (.-left parentRect))
                                     context-y (- (.-clientY %) (.-top parentRect))]
                                 (swap! circle-drawer-state
                                        (fn [state]
                                          (assoc state :context-menu
                                                 (some
                                                  (fn [[idx circle]]
                                                    (when (in-circle? circle [context-x context-y])
                                                      (merge {:type :show :idx idx} circle)))
                                                  (map-indexed vector (circles state))))))))]
    (r/create-class
     {:component-did-mount
      (fn []
        (.addEventListener js/window "click" outside-click-handler)
        (.addEventListener @container "contextmenu" context-menu-handler))
      :component-will-unmount
      (fn []
        (.removeEventListener js/window "click" outside-click-handler)
        (.removeEventListener @container "contextmenu" context-menu-handler))
      :reagent-render
      (fn []
        (let [{:keys [actions inactive-actions context-menu]} @circle-drawer-state
              circles (circles @circle-drawer-state)]
          [:<>
           [:div.flex.justify-center.mb-3
            [button
             {:class "mx-3"
              :disabled (= (count actions) inactive-actions)
              :on-click #(swap! circle-drawer-state update :inactive-actions inc)}
             "Undo"]
            [button
             {:class "mx-3"
              :disabled (= inactive-actions 0)
              :on-click #(swap! circle-drawer-state update :inactive-actions dec)}
             "Redo"]]
           [:div.relative
            (case (:type context-menu)
              :show
              (let [{:keys [x y r idx]} context-menu]
                [:div.absolute.bg-gray-200.z-10.p-3.border.rounded.w-40
                 {:style {:top y :left x} :on-click #(.stopPropagation %)}
                 [:button {:on-click #(swap! circle-drawer-state assoc :context-menu {:type :update-circle :idx idx :x x :y y :r r})} "Adjust diameter.."]])
              :update-circle
              (let [{:keys [x y r]} context-menu]
                [:div.absolute.bg-gray-200.z-10.p-3.border.rounded.w-56
                 {:style {:top y :left x} :on-click #(.stopPropagation %)}
                 [:label (str "Adjust diameter of circle at (" (js/Math.round x) "," (js/Math.round y) ").")]
                 [input-range {:value r :min 0 :max 200
                               :on-change #(swap! circle-drawer-state update :context-menu assoc :r (js/parseInt %))}]])
              nil)
            [:div.bg-white.mx-3.border.rounded.overflow-hidden.relative
             {:ref #(reset! container %)
              :on-click #(let [parentRect (.getBoundingClientRect @container)
                               x (- (.-clientX %) (.-left parentRect))
                               y (- (.-clientY %) (.-top parentRect))]
                           (when (and (not context-menu)
                                      (every? (fn [circle] (not (in-circle? circle [x y]))) circles))
                             (add-action {:type :create
                                          :x x
                                          :y y
                                          :r 25})))
              :style {:width "20rem" :height "15rem"}}
             (map-indexed
              (fn [idx {:keys [x y r]}]
                (let [r (if (= (:idx context-menu) idx) (:r context-menu) r)]
                  [:div.absolute.rounded-full.border-2.hover:bg-gray-100
                   {:key idx
                    :style {:transform (str "translate(-50%,-50%)")
                            :top y
                            :left x
                            :width (* r 2)
                            :height (* r 2)}}]))
              circles)]]]))})))

(defn app []
  [:div.p-6.container.mx-auto.text-gray-900
   [:h1.text-2xl.font-semibold.mb-8
    [:a {:href "https://eugenkiss.github.io/7guis/tasks"} "7GUIs in Clojure/Reagent"]]
   [:div.flex.flex-col.items-start
    [task-container {:title "Counter"} [counter]]
    [task-container {:title "Temperature Converter"} [temperature-converter]]
    [task-container {:title "Flight Booker"} [flight-booker]]
    [task-container {:title "Timer"} [timer]]
    [task-container {:title "CRUD"} [crud]]
    [task-container {:title "Circle Drawer"} [circle-drawer]]]])

(dom/render
 [app]
 (js/document.getElementById "root"))
