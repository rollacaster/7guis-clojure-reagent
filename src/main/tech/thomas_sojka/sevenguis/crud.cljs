(ns tech.thomas-sojka.sevenguis.crud
  (:require [clojure.string :as str]
            [tech.thomas-sojka.sevenguis.components :refer [button input]]
            [reagent.core :as r]))

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
       [:div.flex.mb-3.flex-col.md:flex-row
        [:label {:class "w-1/2" :for "crud-name"} "Name:"]
        [input {:class "md:w-32" :id "crud-name"
                :value name
                :on-change #(swap! crud-state assoc :name %)}]]
       [:div.flex.flex-col.md:flex-row
        [:label {:class "w-1/2" :for "crud-surname"} "Surname:"]
        [input {:class "md:w-32" :id "crud-surname"
                :value surname
                :on-change #(swap! crud-state assoc :surname %)}]]]]
     [:div.flex
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

