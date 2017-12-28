(ns show-updates.events
  (:require [ajax.core             :as ajax]
            [day8.re-frame.http-fx]
            [re-frame.core         :as rf]))

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

