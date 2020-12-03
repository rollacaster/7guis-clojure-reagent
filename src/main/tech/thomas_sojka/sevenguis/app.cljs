(ns tech.thomas-sojka.sevenguis.app
  (:require [reagent.dom :as dom]
            [reagent.core :as r]))

(defn task-container [{:keys [title]} children]
  [:div.shadow-lg.mb-6
   [:header.bg-gray-200.text-center.rounded-t.border-t.border-l.border-r.border-gray-400
    title]
   [:main.bg-gray-100.p-2.rounded-b.border.border-gray-400
    children]])

(defn counter []
  (let [count (r/atom 0)]
    (fn []
      [:div.flex
       [:div.text-center.px-10.w-5
        @count]
       [:div
        [:button.bg-gray-200.px-5.rounded.border.border-gray-700
         {:on-click #(swap! count inc)} "Count"]]])))

(defn input [{:keys [value on-change class disabled id]}]
  [:input.mr-3.rounded.px-2.border
   {:value value :class class :on-change (fn [e] (on-change ^js (.-target.value e)))
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
            :class (r/class-names
                    (when (= (:error @temperatures) :celsius) "bg-red-200")
                    (when (= (:error @temperatures) :fahrenheit) "bg-gray-500"))
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
            :class (r/class-names
                    (when (= (:error @temperatures) :fahrenheit) "bg-red-200")
                    (when (= (:error @temperatures) :celsius) "bg-gray-500"))
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

(defn app []
  [:div.p-6.container.mx-auto.text-gray-900
   [:h1.text-2xl.font-semibold.mb-8
    "7GUIs in Clojure/Reagent"]
   [:div.flex.flex-col.items-start
    [task-container {:title "Counter"} [counter]]
    [task-container {:title "Temperature Converter"} [temperature-converter]]]])

(dom/render
 [app]
 (js/document.getElementById "root"))
