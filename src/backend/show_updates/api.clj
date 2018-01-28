(ns show-updates.api
  "A REST API for interacting with the local database and TVmaze API."
  (:require [clj-time.core         :as t]
            [clj-time.format       :as f]
            [show-updates.database :as db]
            [show-updates.tvmaze   :as tv]
            [yada.yada             :as yada]))

(defn shows
  "Returns shows with at least one unwatched episode."
  [ctx]
  (db/query "SELECT DISTINCT s.name,s.tvmazeid,s.bookmark,s.imageurl
             FROM show AS s
             INNER JOIN episode AS e ON e.showid = s.tvmazeid"))

(defn episodes
  [ctx]
  (db/query ["SELECT name,season,number,airdate,summary,imageurl
              FROM episode
              WHERE showid = ?"
             (get-in ctx [:parameters :query :showid])]))

(defn show-search
  [ctx]
  (tv/show-search (get-in ctx [:parameters :query :query])))

(defn add-show!
  [{:keys [parameters] :as ctx}]
  (let [{:keys [tvmazeid]}                        (:body parameters)
        {:strs [id name summary premiered image]} (tv/show tvmazeid)
        bookmark (-> (f/parse (or premiered "1900-02-01"))
                     (t/minus (t/days 1)))
        show     {:tvmazeid id
                  :name     name
                  :summary  summary
                  :bookmark bookmark
                  :imageurl (get image "medium")}
        episodes (->> (tv/unwatched-episodes id bookmark)
                      (map
                        (fn [{:strs [name airdate season number summary image]}]
                          {:showid   id
                           :name     name
                           :airdate  airdate
                           :season   season
                           :number   number
                           :summary  summary
                           :imageurl (get image "medium")})))]
    (db/with-transaction
      (db/insert! :show show)
      (doseq [episode episodes]
        (db/insert! :episode episode)))
    (assoc show :episodes episodes)))

(defn bookmark!
  [{:keys [parameters] :as ctx}]
  (let [{:keys [showid bookmark]} (:body parameters)
        ;; Move bookmark to just after the airdate of the episode last watched.
        bookmark (as-> bookmark ?
                   (f/parse (f/formatter "yyyy-MM-dd") ?)
                   (t/plus ? (t/hours 1)))]
    (db/with-transaction
      (db/execute! ["UPDATE show
                     SET bookmark = ?
                     WHERE tvmazeid = ?"
                    bookmark
                    showid])
      (db/execute! ["DELETE FROM episode
                      WHERE showid = ?
                        AND airdate <= date(?)"
                    showid
                    ;; YYYY-MM-DD format is required by SQLite.
                    (f/unparse (f/formatter "yyyy-MM-dd") bookmark)]))
    {:result "Bookmark successfully set."}))

(defn resource
  "Returns a yada resource with some common configuration:
   - CORS configured to allow access to all origins
   - Server errors returned as JSON in the event of a 500."
  [m]
  (yada/resource
    (merge m {:access-control {:allow-origin  "*"
                               :allow-headers "*"}
              :responses {500 {:description "Server error"
                               :produces "application/json"
                               :response (fn [{:keys [error] :as ctx}]
                                           {:error error})}}})))

(def routes
  [""
   [
    ["/shows"       (resource
                      {:methods
                       {:get
                        {:produces "application/json"
                         :response shows}}})]
    ["/show-search" (resource
                      {:methods
                       {:get
                        {:parameters {:query {:query String}}
                         :produces "application/json"
                         :response show-search}}})]
    ["/add-show"    (resource
                      {:methods
                       {:post
                        {:parameters {:body {:tvmazeid Long}}
                         :consumes   "application/json"
                         :produces   "application/json"
                         :response   add-show!}}})]
    ["/episodes"    (resource
                      {:methods
                       {:get
                        {:parameters {:query {:showid Long}}
                         :consumes   "application/json"
                         :produces   "application/json"
                         :response   episodes}}})]
    ["/bookmark"    (resource
                      {:methods
                       {:post
                        {:parameters {:body {:showid   Long
                                             :bookmark String}}
                         :consumes   "application/json"
                         :produces   "application/json"
                         :response   bookmark!}}})]
    ]])

(defn start-server!
  "Starts a server on the provided `port`.

   Returns a map that includes:

     :port    the port being used
     :close   a function that will stop the server
     :server  a reference to the server"
  [port]
  (yada/listener routes {:port port}))
