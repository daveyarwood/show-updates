(ns show-updates.events
  (:require [ajax.core             :as ajax]
            [day8.re-frame.http-fx]
            [re-frame.core         :as rf]))

(rf/reg-event-db
  :initialize
  (fn [_ _]
    {:shows []
     :add-show-form {:form-visible? false}}))

(rf/reg-event-db
  :bad-response
  (fn [db [_ response]]
    (-> db
        (assoc :loading? false)
        (assoc :failure (js->clj response)))))

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
  (fn [{:keys [db]} [_ {:keys [tvmazeid] :as show}]]
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

(rf/reg-event-fx
  :mark-watched
  (fn [{:keys [db]} [_ tvmazeid airdate]]
    {:db         (assoc db :loading? true)
     :http-xhrio {:method          :post
                  ;; TODO: load server location from environment
                  :uri             "http://localhost:12345/bookmark"
                  :params          {:showid   tvmazeid
                                    :bookmark airdate}
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format
                                     {:keywords? true})
                  :on-success      [:load-show {:tvmazeid tvmazeid}]
                  :on-failure      [:bad-response]}}))

(rf/reg-event-fx
  :show-search
  (fn [{:keys [db]} [_ query]]
    {:db         (assoc-in db [:add-show-form :query] query)
     :http-xhrio {:method          :get
                  ;; TODO: load server location from environment
                  :uri             "http://localhost:12345/show-search"
                  :params          {:query query}
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format
                                     {:keywords? true})
                  :on-success      [:process-search-results]
                  :on-failure      [:bad-response]}}))

(rf/reg-event-fx
  :add-show
  (fn [{:keys [db]} [_ tvmazeid]]
    {:db         db
     :http-xhrio {:method          :post
                  ;; TODO: load server location from environment
                  :uri             "http://localhost:12345/add-show"
                  :params          {:tvmazeid tvmazeid}
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format
                                     {:keywords? true})
                  :on-success      [:process-add-show-success]
                  :on-failure      [:bad-response]}}))

(rf/reg-event-db
  :process-shows
  (fn [{:keys [db]} [_ response]]
    (-> db
        (assoc :loading? false)
        (assoc :shows (js->clj response)))))

(rf/reg-event-db
  :process-episodes
  (fn [db [_ response]]
    (-> db
        (assoc :loading? false)
        (assoc-in [:show :episodes] (js->clj response)))))

(rf/reg-event-db
  :process-search-results
  (fn [db [_ response]]
    (-> db
        (assoc-in [:add-show-form :search-results] (js->clj response)))))

(rf/reg-event-db
  :process-add-show-success
  (fn [db [_ {:keys [name] :as response}]]
    (rf/dispatch [:load-shows])
    (assoc
      db
      :success-message (str name " added to shows.")
      :shows           []
      :add-show-form   {:form-visible? false})))

(rf/reg-event-db
  :display-add-show-form
  (fn [db _]
    (assoc-in db [:add-show-form :form-visible?] true)))

(rf/reg-event-db
  :hide-add-show-form
  (fn [db _]
    (assoc-in db [:add-show-form :form-visible?] false)))

(rf/reg-event-db
  :clear-success-message
  (fn [db _]
    (dissoc db :success-message)))
