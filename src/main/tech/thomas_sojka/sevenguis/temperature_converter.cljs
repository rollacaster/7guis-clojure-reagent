(ns tech.thomas-sojka.sevenguis.temperature-converter
  (:require [tech.thomas-sojka.sevenguis.components :refer [input]]
            [reagent.core :as r]))

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
