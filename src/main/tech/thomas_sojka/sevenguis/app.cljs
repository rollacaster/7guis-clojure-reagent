(ns tech.thomas-sojka.sevenguis.app
  (:require [tech.thomas-sojka.sevenguis.counter :refer [counter]]
            [reagent.dom :as dom]
            [tech.thomas-sojka.sevenguis.cells :refer [cells]]
            [tech.thomas-sojka.sevenguis.circle-drawer :refer [circle-drawer]]
            [tech.thomas-sojka.sevenguis.crud :refer [crud]]
            [tech.thomas-sojka.sevenguis.flight-booker :refer [flight-booker]]
            [tech.thomas-sojka.sevenguis.temperature-converter
             :refer
             [temperature-converter]]
            [tech.thomas-sojka.sevenguis.timer :refer [timer]]))

(defn task-container [{:keys [title]} children]
  [:div.shadow-lg.mb-6
   [:header.bg-gray-200.text-center.rounded-t.border-t.border-l.border-r.border-gray-400
    title]
   [:main.bg-gray-100.p-2.rounded-b.border.border-gray-400
    children]])


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
    [task-container {:title "Circle Drawer"} [circle-drawer]]
    [task-container {:title "Cells"} [cells]]]])

(dom/render
 [app]
 (js/document.getElementById "root"))
