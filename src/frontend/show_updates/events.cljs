(ns show-updates.events
  (:require [ajax.core             :as ajax]
            [day8.re-frame.http-fx]
            [re-frame.core         :as rf]))

(rf/reg-event-db
  :initialize
  (fn [_ _]
    {:shows []}))

(rf/reg-event-fx
  :load-shows
  (fn [db _]
    {:db         (assoc db :loading? true)
     :http-xhrio {:method          :get
                  ;; TODO: load server location from environment
                  :uri             "http://localhost:12345/shows"
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format
                                     {:keywords? true})
                  :on-success      [:process-shows]
                  :on-failure      [:bad-response]}}))

(rf/reg-event-fx
  :load-show
  (fn [db [_ {:keys [tvmazeid] :as show}]]
    {:db         (assoc db :loading? true
                           :show     show)
     :http-xhrio {:method          :get
                  ;; TODO: load server location from environment
                  :uri             "http://localhost:12345/episodes"
                  :params          {:showid tvmazeid}
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format
                                     {:keywords? true})
                  :on-success      [:process-episodes]
                  :on-failure      [:bad-response]}}))

(rf/reg-event-db
  :process-shows
  (fn [db [_ response]]
    (js/console.log "process-shows response" (clj->js response))
    (js/console.log (clj->js db))
    (-> db
        (assoc :loading? false)
        (assoc :shows (js->clj response)))))

(rf/reg-event-db
  :process-episodes
  (fn [db [_ response]]
    (js/console.log "process-episodes response" (clj->js response))
    (js/console.log (clj->js db))
    (-> db
        (assoc :loading? false)
        (assoc-in [:show :episodes] (js->clj response)))))

(rf/reg-event-db
  :bad-response
  (fn [db [_ response]]
    (-> db
        (assoc :loading? false)
        (assoc :failure (js->clj response)))))

