(ns show-updates.subscriptions
  (:require [re-frame.core :as rf]))

(rf/reg-sub
  :db
  (fn [db _] db))

(rf/reg-sub
  :shows
  (fn [{:keys [data] :as db} _]
    (:shows data)))

