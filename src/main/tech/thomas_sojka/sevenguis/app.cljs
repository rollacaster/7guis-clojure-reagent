(ns tech.thomas-sojka.sevenguis.app
  (:require [reagent.dom :as dom]
            [reagent.core :as r]))

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
                                  "bg-gray-200 border-gray-700"))
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

(defn app []
  [:div.p-6.container.mx-auto.text-gray-900
   [:h1.text-2xl.font-semibold.mb-8
    [:a {:href "https://eugenkiss.github.io/7guis/tasks"} "7GUIs in Clojure/Reagent"]]
   [:div.flex.flex-col.items-start
    [task-container {:title "Counter"} [counter]]
    [task-container {:title "Temperature Converter"} [temperature-converter]]
    [task-container {:title "Flight Booker"} [flight-booker]]]])

(dom/render
 [app]
 (js/document.getElementById "root"))
