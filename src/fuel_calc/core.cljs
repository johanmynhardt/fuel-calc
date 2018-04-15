(ns fuel-calc.core
    (:require [rum.core :as rum]
              [cljs.pprint :as pprint]))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {
                          :text "Hello world!"
                          :distance 10.0
                          :price 10.0
                          :consumption 7.0
                          :people 1}))

;; TODO: curl "http://widget-carburant.syspark.net/services?country=ZA_ZA"

(comment 
curl "http://widget-carburant.syspark.net/services?country=ZA_ZA"



)

(defn update! [key val]
  (swap! app-state assoc key val))

(defn el-val [el]
  (.. el -target -value))

(defn format-decimal [input]
  (pprint/cl-format nil "~,2f" input))

(rum/defc input-value-changer < rum/reactive [key fun]
  ;(println (str "value changer for atom " (rum/react val-atom)))
  (let []
    [:div {:class "input-group"}
     [:button {:on-click (fn []
                           (let [new-val (fun (key @app-state))]
                             (update! key new-val)))}
      "inc"]
     ;[:button {:on-click #(update! key (inc (key @app-state)))} "inc"]
                                        ;[:button {:on-click #(swap! val-atom inc)} "+"]
                                        ;[:button {:on-click #(swap! val-atom dec)} "-"]
     ]))

(rum/defc x < rum/reactive [st]
  [:button {:on-change (fn [] (update! :distance (inc (:distance @st))))
            :onDragStart (fn [e] (println (str "dragging... " e)))}
   "click me!"
   ])

(rum/defc calculated < rum/reactive []
  (let [{:keys [price distance people consumption] :as st} (rum/react app-state)
        litres-used (/ distance (/ 100 consumption))
        fuel-cost (* price litres-used)
        fuel-cost-pp (/ fuel-cost people)]
    [:div
     [:div  "Litres used: " (format-decimal litres-used)]
     [:div "Fuel cost(TOTAL): " (format-decimal fuel-cost)]
     [:div "Fuel cost(PER/PP): " (format-decimal fuel-cost-pp)]]))

(rum/defc root < rum/reactive [app-state]
  (let [{:keys [consumption distance people price] :as st} (rum/react app-state)]
    [:div
     (comment  (x app-state) [:div {
                                    :style {:axis "y"}
                                    :on-touch-move (fn [e] (println (.-clientX (.item (.-touches e) 0))))
                                    
                                    
                                    }  "(dist: " distance ")"])
     [:div {:class "input-group"}
      
      [:label "Distance " [:br]
       [:input {:type "number"
                :value distance
                :step "0.1"
                :min 1.0
                :onChange #(update! :distance (el-val %))}]
       " km"]
      ;(input-value-changer :distance inc)
      ]

     [:div {:class "input-group"}
      [:label "Price " [:br]
       [:input {:type "number"
                :min 0
                :max 20
                :step 0.1
                :value price
                :onChange #(update! :price (el-val %))}]
       " /L"]]

     [:div {:class "input-group"}
      [:label "Consumption" [:br] 
       [:input {:type "range"
                :min 4
                :max 10
                :step 0.1
                :value consumption
                :onChange #(update! :consumption (el-val %))}]
       [:br]
       consumption "L/100 km "
       [:br] (format-decimal (/ 100 consumption)) " km/L"]]

     [:div {:class "input-group"}
      [:label "Persons" [:br]
       [:input {:type "Range"
                :min 1
                :max 7
                :value people
                :onChange #(update! :people (el-val %))}]
       [:br] people ]]

     [:div.input-group
      [:strong  "Result"] (calculated)]
     ]))

(defn mountroot! [el-id]
  (rum/hydrate (root app-state) (js/document.getElementById el-id)))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
(mountroot! "app")
)

(mountroot! "app")
