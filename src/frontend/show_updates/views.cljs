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
   (into
     [:div]
     (cons
       [:h1 "Shows"]
       (for [{:keys [name] :as show} @(rf/subscribe [:shows])]
         [:div
          [:a {:href "#"
               :on-click #(rf/dispatch [:load-show show])}
           name]])))])

(defn show-component
  []
  (into
    [:div]
    (when-let [{:keys [name bookmark imageurl episodes]} @(rf/subscribe [:show])]
      [[:h1 name]
       [:pre (with-out-str (pprint episodes))]])))

(defn content-component
  []
  [:div
   [shows-component]
   [show-component]])

(defn app-component
  []
  [:div
   (let [{:keys [loading? failure]} @(rf/subscribe [:db])]
     (cond
       loading? [loading-component]
       failure  [failure-component]
       :else    [content-component]))])

