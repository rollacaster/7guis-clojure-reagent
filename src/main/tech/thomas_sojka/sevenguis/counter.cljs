(ns tech.thomas-sojka.sevenguis.counter
  (:require [tech.thomas-sojka.sevenguis.components :refer [button]]
            [reagent.core :as r]))

(defn counter []
  (let [count (r/atom 0)]
    (fn []
      [:div.flex
       [:div.text-center.px-10.w-5
        @count]
       [:div
        [button
         {:on-click #(swap! count inc)} "Count"]]])))
