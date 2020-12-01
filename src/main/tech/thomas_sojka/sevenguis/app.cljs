(ns tech.thomas-sojka.sevenguis.app
  (:require [reagent.dom :as dom]))

(defn app []
  [:div.p-6.container.mx-auto
   [:h1.text-2xl.font-semibold "7GUIs in Clojure/Reagent"]])

(dom/render
 [app]
 (js/document.getElementById "root"))
