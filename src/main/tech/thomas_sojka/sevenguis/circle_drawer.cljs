(ns tech.thomas-sojka.sevenguis.circle-drawer
  (:require [tech.thomas-sojka.sevenguis.components
             :refer
             [button input-range]]
            [reagent.core :as r]))

(def circle-drawer-state
  (r/atom {:actions []
           :inactive-actions 0
           :context-menu nil}))


(defn circle-drawer-reducer [actions]
  (reduce
   (fn [state {:keys [type] :as action}]
     (case type
       :create (conj state {:x (:x action) :y (:y action) :r (:r action)})
       :radius (map
                (fn [{:keys [x y] :as circle}]
                  (if (and (= x (:x action))
                           (= y (:y action)))
                    (assoc circle :r (:r action))
                    circle))
                state)))
   []
   (reverse actions)))

(defn add-action [action]
  (swap! circle-drawer-state
         (fn [{:keys [actions inactive-actions] :as state}]
           (-> state
               (assoc :actions (conj (drop inactive-actions actions) action))
               (assoc :inactive-actions 0)
               (assoc :context-menu nil)))))

(defn circles [{:keys [actions inactive-actions]}]
  (circle-drawer-reducer (drop inactive-actions actions)))

(defn in-circle? [{:keys [x y r]} [test-x test-y]]
  (and (< (- x r) test-x) (> (+ x r) test-x)
       (< (- y r) test-y) (> (+ y r) test-y)))

(def circle-drawer
  (let [container (atom nil)
        outside-click-handler (fn []
                                (let [{:keys [context-menu]} @circle-drawer-state]
                                  (case (:type context-menu)
                                    :show
                                    (swap! circle-drawer-state assoc :context-menu nil)
                                    :update-circle
                                    (let [{:keys [x y r]} context-menu]
                                      (add-action {:type :radius :x x :y y :r r}))
                                    nil)))
        context-menu-handler #(do
                               (.preventDefault %)
                               (let [parentRect (.getBoundingClientRect @container)
                                     context-x (- (.-clientX %) (.-left parentRect))
                                     context-y (- (.-clientY %) (.-top parentRect))]
                                 (swap! circle-drawer-state
                                        (fn [state]
                                          (assoc state :context-menu
                                                 (some
                                                  (fn [[idx circle]]
                                                    (when (in-circle? circle [context-x context-y])
                                                      (merge {:type :show :idx idx} circle)))
                                                  (map-indexed vector (circles state))))))))]
    (r/create-class
     {:component-did-mount
      (fn []
        (.addEventListener js/window "click" outside-click-handler)
        (.addEventListener @container "contextmenu" context-menu-handler))
      :component-will-unmount
      (fn []
        (.removeEventListener js/window "click" outside-click-handler)
        (.removeEventListener @container "contextmenu" context-menu-handler))
      :reagent-render
      (fn []
        (let [{:keys [actions inactive-actions context-menu]} @circle-drawer-state
              circles (circles @circle-drawer-state)]
          [:<>
           [:div.flex.justify-center.mb-3
            [button
             {:class "mx-3"
              :disabled (= (count actions) inactive-actions)
              :on-click #(swap! circle-drawer-state update :inactive-actions inc)}
             "Undo"]
            [button
             {:class "mx-3"
              :disabled (= inactive-actions 0)
              :on-click #(swap! circle-drawer-state update :inactive-actions dec)}
             "Redo"]]
           [:div.relative
            (case (:type context-menu)
              :show
              (let [{:keys [x y r idx]} context-menu]
                [:div.absolute.bg-gray-200.z-10.p-3.border.rounded.w-40
                 {:style {:top y :left x} :on-click #(.stopPropagation %)}
                 [:button {:on-click #(swap! circle-drawer-state assoc :context-menu {:type :update-circle :idx idx :x x :y y :r r})} "Adjust diameter.."]])
              :update-circle
              (let [{:keys [x y r]} context-menu]
                [:div.absolute.bg-gray-200.z-10.p-3.border.rounded.w-56
                 {:style {:top y :left x} :on-click #(.stopPropagation %)}
                 [:label (str "Adjust diameter of circle at (" (js/Math.round x) "," (js/Math.round y) ").")]
                 [input-range {:value r :min 0 :max 200
                               :on-change #(swap! circle-drawer-state update :context-menu assoc :r (js/parseInt %))}]])
              nil)
            [:div.bg-white.mx-3.border.rounded.overflow-hidden.relative
             {:ref #(reset! container %)
              :on-click #(let [parentRect (.getBoundingClientRect @container)
                               x (- (.-clientX %) (.-left parentRect))
                               y (- (.-clientY %) (.-top parentRect))]
                           (when (and (not context-menu)
                                      (every? (fn [circle] (not (in-circle? circle [x y]))) circles))
                             (add-action {:type :create
                                          :x x
                                          :y y
                                          :r 25})))
              :style {:width "20rem" :height "15rem"}}
             (map-indexed
              (fn [idx {:keys [x y r]}]
                (let [r (if (= (:idx context-menu) idx) (:r context-menu) r)]
                  [:div.absolute.rounded-full.border-2.hover:bg-gray-100
                   {:key idx
                    :style {:transform (str "translate(-50%,-50%)")
                            :top y
                            :left x
                            :width (* r 2)
                            :height (* r 2)}}]))
              circles)]]]))})))
