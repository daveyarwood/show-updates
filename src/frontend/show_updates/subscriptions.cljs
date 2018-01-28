(ns show-updates.subscriptions
  (:require [re-frame.core :as rf]))

(rf/reg-sub
  :db
  (fn [db _] db))

(rf/reg-sub
  :shows
  (fn [{:keys [shows] :as db} _]
    shows))

(rf/reg-sub
  :show
  (fn [{:keys [show] :as db} _]
    show))

(rf/reg-sub
  :episodes
  (fn [{:keys [show] :as db} _]
    (:episodes show)))

(rf/reg-sub
  :add-show-form
  (fn [{:keys [add-show-form] :as db} _]
    add-show-form))

(rf/reg-sub
  :success-message
  (fn [{:keys [success-message] :as db} _]
    success-message))
