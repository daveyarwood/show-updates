(ns show-updates.app
  (:require [ajax.core             :as ajax]
            [cljs.pprint           :refer (pprint)]
            [day8.re-frame.http-fx]
            [reagent.core          :as reagent]
            [re-frame.core         :as rf]))

;; event handlers

(rf/reg-event-db
  :initialize
  (fn [_ _]
    {:data {:shows []}}))

(rf/reg-event-fx
  :load-data
  (fn [db _]
    {:db         (assoc db :loading? true)
     :http-xhrio {:method          :get
                  ;; TODO: load server location from environment
                  :uri             "http://localhost:12345/shows"
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format
                                     {:keywords? true})
                  :on-success      [:process-response]
                  :on-failure      [:bad-response]}}))

(rf/reg-event-db
  :process-response
  (fn [db [_ response]]
    (-> db
        (assoc :loading? false)
        (update :data assoc :shows (js->clj response)))))

(rf/reg-event-db
  :bad-response
  (fn [db [_ response]]
    (-> db
        (assoc :loading? false)
        (assoc :failure (js->clj response)))))

;; subscriptions

(rf/reg-sub
  :db
  (fn [db _] db))

(rf/reg-sub
  :shows
  (fn [{:keys [data] :as db} _]
    (:shows data)))

;; view components

(defn loading-component
  []
  [:em "Loading..."])

(defn failure-component
  []
  [:div
   [:h1 "ERROR"]
   [:pre (-> @(rf/subscribe [:db])
             :failure
             pprint
             with-out-str)]])

(defn shows-component
  []
  [:div
   [:h1 "Shows"]
   (into
     [:div]
     (for [{:keys [name tvmazeid bookmark imageurl]}
           @(rf/subscribe [:shows])]
       [:div
        [:h2 name]
        [:img {:src imageurl}]]))])

(defn content-component
  []
  [:div [shows-component]])

(defn app-component
  []
  [:div
   (let [{:keys [loading? failure]} @(rf/subscribe [:db])]
     (cond
       loading? [loading-component]
       failure  [failure-component]
       :else    [content-component]))])

;; entrypoint

(defn init
  []
  (rf/dispatch-sync [:initialize])
  (rf/dispatch [:load-data])
  (reagent/render-component
    [app-component]
    (.getElementById js/document "app")))
