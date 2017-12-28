(ns show-updates.api
  "A REST API for interacting with the local database and TVmaze API."
  (:require [clj-time.core         :as t]
            [clj-time.format       :as f]
            [show-updates.database :as db]
            [show-updates.tvmaze   :as tv]
            [yada.yada             :as yada]))

(defn shows
  [ctx]
  (db/query "SELECT name,tvmazeid,bookmark,imageurl FROM show"))

(defn episodes
  [ctx]
  (db/query "SELECT name,season,number,airdate,summary,imageurl
             FROM episodes
             WHERE showid = ?"
            [(get-in ctx [:parameters :query :showid])]))

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

(defn public-resource
  "Returns a yada resource with CORS configured to allow access to all origins."
  [m]
  (yada/resource (merge m {:access-control {:allow-origin "*"}})))

(def routes
  [""
   [
    ["/shows"       (public-resource
                      {:methods
                       {:get
                        {:produces "application/json"
                         :response shows}}})]
    ["/show-search" (public-resource
                      {:methods
                       {:get
                        {:parameters {:query {:query String}}
                         :produces "application/json"
                         :response show-search}}})]
    ["/add-show"    (public-resource
                      {:methods
                       {:put
                        {:parameters {:body {:tvmazeid Long}}
                         :consumes   "application/json"
                         :produces   "application/json"
                         :response   add-show!}}})]
    ["/episodes"    (public-resource
                      {:methods
                       {:get
                        {:parameters {:query {:showid Long}}
                         :consumes   "application/json"
                         :produces   "application/json"
                         :response   episodes}}})]
    ]])

(defn start-server!
  "Starts a server on the provided `port`.

   Returns a map that includes:

     :port    the port being used
     :close   a function that will stop the server
     :server  a reference to the server"
  [port]
  (yada/listener routes {:port port}))
