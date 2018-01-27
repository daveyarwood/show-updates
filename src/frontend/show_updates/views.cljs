(ns show-updates.views
  (:require [cljs.pprint        :refer (pprint)]
            [goog.string        :refer (format)]
            [goog.string.format]
            [re-frame.core      :as    rf]))

(defn debug
  [x]
  (->> x pprint with-out-str (vector :pre)))

(defn loading-component
  []
  [:em "Loading..."])

(defn failure-component
  []
  [:div
   [:h1 "ERROR"]
   (let [{:keys [failure]} @(rf/subscribe [:db])]
     (debug failure))])

(defn shows-component
  []
  (into
    [:div]
    (cons
      [:h1 "Shows"]
      (for [{:keys [name] :as show} @(rf/subscribe [:shows])]
        [:div
         [:a {:href "#"
              :on-click #(rf/dispatch [:load-show show])}
          name]]))))

(defn show-component
  []
  (into
    [:div]
    (when-let [{:keys [tvmazeid name bookmark imageurl episodes]}
               @(rf/subscribe [:show])]
      (cons
        [:h2 name]
        (for [{:keys [name season number airdate summary]} episodes]
          [:div
           [:div
            [:a
             {:href "#"
              :on-click
              #(if (js/confirm
                     "Mark this episode (and all prior episodes) watched?")
                 (rf/dispatch [:mark-watched tvmazeid airdate]))}
             (format "S%02dE%02d: %s" season number name)]
            [:p [:em "Airdate: " airdate]]
            [:p {:dangerouslySetInnerHTML {:__html summary}}]]])))))

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

