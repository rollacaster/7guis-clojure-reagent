(ns tech.thomas-sojka.sevenguis.timer
  (:require [tech.thomas-sojka.sevenguis.components
             :refer
             [button input-range]]
            [reagent.core :as r]))

(defonce duration-state (r/atom {:duration 0
                                 :time 0}))
;; TODO ClearInterval
(defonce interval (js/setInterval
                   #(swap! duration-state
                           (fn [{:keys [time duration] :as state}]
                             (assoc state :time (if (< time duration) (+ time 0.1) time))))
                   100))

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

