(ns show-updates.views
  (:require [cljs.pprint   :refer (pprint)]
            [re-frame.core :as    rf]))

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
        [:a name]
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

