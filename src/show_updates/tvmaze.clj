(ns show-updates.tvmaze
  "A library of functions that use the TVmaze API to obtain information needed
   by our application."
  (:require [clj-http.client :as http]
            [clj-time.core   :as t]
            [clj-time.format :as f]
            [cheshire.core   :as json]))

(def ^:constant TVMAZE-API-BASE "https://api.tvmaze.com")

(defn route
  [endpoint & values]
  (apply format (str TVMAZE-API-BASE endpoint) values))

(defn show-search
  [query]
  (as-> query x
    (http/get (route "/search/shows") {:query-params {"q" x}})
    (:body x)
    (json/parse-string x)
    (for [{:strs [show]} x]
      (select-keys show ["id" "name" "summary" "image"]))))

(defn show
  [id]
  (-> (http/get (route "/shows/%s" id))
      :body
      json/parse-string
      (select-keys ["id" "name" "summary" "image" "premiered"])))

(defn all-episodes
  [show-id]
  (as-> show-id x
    (http/get (route "/shows/%s/episodes" x) {:query-params {"specials" 1}})
    (:body x)
    (json/parse-string x)
    (for [episode x]
      (select-keys episode ["season" "number" "airdate"
                            "name" "summary" "image"]))))

(defn unwatched-episodes
  "Returns a list of episodes that have not been watched, given a bookmark
   DateTime representing the date at which point all episodes before that have
   been watched.

   Filters out episodes that haven't aired yet."
  [show-id bookmark]
  (->> (all-episodes show-id)
       (filter (fn [{:strs [airdate]}]
                 ;; If "airdate" is missing, filter it out by setting it to a
                 ;; date earlier than the bookmark can be.
                 (let [air-date (f/parse (or airdate "1900-01-01"))]
                   (and (t/after? air-date bookmark)
                        (t/before? air-date (t/now))))))
       (sort-by #(get % "airdate"))))
