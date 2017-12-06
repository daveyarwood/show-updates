(ns show-updates.tvmaze
  "A library of functions that use the TVmaze API to obtain information needed
   by our application."
  (:require [clj-http.client :as http]
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
      (select-keys ["id" "name" "summary" "image"])))

(defn episodes
  [show-id]
  (as-> show-id x
    (http/get (route "/shows/%s/episodes" x) {:query-params {"specials" 1}})
    (:body x)
    (json/parse-string x)
    (for [episode x]
      (select-keys episode ["season" "number" "airdate"
                            "name" "summary" "image"]))))

