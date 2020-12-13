(ns tech.thomas-sojka.sevenguis.components
  (:require [reagent.core :as r]))

(defn button [{:keys [on-click class disabled]} children]
  [:button.px-5.rounded.border
   {:on-click on-click
    :class (r/class-names class (if disabled
                                  "border-gray-400 text-gray-500"
                                  "bg-gray-200 border-gray-500"))
    :disabled disabled}
   children])


(defn input [{:keys [value on-change class disabled id error]}]
  [:input.mr-3.rounded.px-2.border
   {:value value :class (r/class-names class (when error "bg-red-200"))
    :on-change (fn [e] (on-change ^js (.-target.value e)))
    :disabled disabled :id id}])

(defn input-range [{:keys [value on-change min max]}]
  [:<>
   [:style "@media screen and (-webkit-min-device-pixel-ratio: 0) {
              input[type=\"range\"]::-webkit-slider-thumb {
                width: 15px;
                -webkit-appearance: none;
                appearance: none;
                height: 15px;
                cursor: ew-resize;
                background: #63b3ed;
                border-radius: 50%;
              }
            }"]
   [:input.w-full.appearance-none.bg-gray-400.h-3.rounded.border.border-gray-500
    {:type "range" :min min :max max :value value :on-change #(on-change ^js (.-target.value %))}]])


