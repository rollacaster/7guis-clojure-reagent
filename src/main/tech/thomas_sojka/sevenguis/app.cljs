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

(defn task-container [{:keys [title link]} children]
  [:div.shadow-lg.mb-6
   [:header.bg-gray-200.text-center.rounded-t.border-t.border-l.border-r.border-gray-400
    [:a.underline {:href link} title]]
   [:main.bg-gray-100.p-2.rounded-b.border.border-gray-400
    children]])


(defn app []
  [:div.p-6.container.mx-auto.text-gray-900
   [:h1.text-2xl.font-semibold.mb-8
    [:a {:href "https://eugenkiss.github.io/7guis/tasks"} "7GUIs in Clojure/Reagent"]]
   [:div.flex.flex-col.items-start
    [task-container
     {:title "Counter"
      :link "https://github.com/rollacaster/7guis-clojure-reagent/blob/main/src/main/tech/thomas_sojka/sevenguis/counter.cljs"}
     [counter]]
    [task-container
     {:title "Temperature Converter"
      :link "https://github.com/rollacaster/7guis-clojure-reagent/blob/main/src/main/tech/thomas_sojka/sevenguis/temperature_converter.cljs"}
     [temperature-converter]]
    [task-container
     {:title "Flight Booker"
      :link "https://github.com/rollacaster/7guis-clojure-reagent/blob/main/src/main/tech/thomas_sojka/sevenguis/flight-booker.cljs"}
     [flight-booker]]
    [task-container
     {:title "Timer"
      :link "https://github.com/rollacaster/7guis-clojure-reagent/blob/main/src/main/tech/thomas_sojka/sevenguis/timer.cljs"}
     [timer]]
    [task-container
     {:title "CRUD"
      :link "https://github.com/rollacaster/7guis-clojure-reagent/blob/main/src/main/tech/thomas_sojka/sevenguis/crud.cljs"}
     [crud]]
    [task-container
     {:title "Circle Drawer"
      :link "https://github.com/rollacaster/7guis-clojure-reagent/blob/main/src/main/tech/thomas_sojka/sevenguis/circle_drawer.cljs"}
     [circle-drawer]]
    [task-container
     {:title "Cells"
      :link "https://github.com/rollacaster/7guis-clojure-reagent/blob/main/src/main/tech/thomas_sojka/sevenguis/cells.cljs"}
     [cells]]]])

(dom/render
 [app]
 (js/document.getElementById "root"))
