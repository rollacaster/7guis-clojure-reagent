(ns tech.thomas-sojka.sevenguis.flight-booker
  (:require [tech.thomas-sojka.sevenguis.components :refer [button input]]
            [reagent.core :as r]))

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
