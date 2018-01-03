(ns show-updates.app
  (:require [reagent.core               :as    reagent]
            [re-frame.core              :as    rf]
            [show-updates.events]
            [show-updates.subscriptions]
            [show-updates.views         :as    views]))

(defn init
  []
  (rf/dispatch-sync [:initialize])
  (rf/dispatch [:load-shows])
  (reagent/render-component
    [views/app-component]
    (.getElementById js/document "app")))
