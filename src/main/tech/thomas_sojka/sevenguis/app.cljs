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

(defn app []
  [:div.p-6.container.mx-auto.text-gray-900
   [:h1.text-2xl.font-semibold.mb-8
    "7GUIs in Clojure/Reagent"]
   [:div.flex
    [task-container {:title "Counter"} [counter]]]])

(dom/render
 [app]
 (js/document.getElementById "root"))
