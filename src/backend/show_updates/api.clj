(ns show-updates.api
  "A REST API for interacting with the local database and TVmaze API."
  (:require [clj-time.core         :as t]
            [clj-time.format       :as f]
            [show-updates.database :as db]
            [show-updates.tvmaze   :as tv]
            [yada.yada             :as yada]))

(defn shows
  [ctx]
  (db/query "SELECT name,tvmazeid FROM show"))

(defn show-search
  [ctx]
  (tv/show-search (get-in ctx [:parameters :query :query])))

(defn add-show!
  [{:keys [parameters] :as ctx}]
  (let [{:keys [tvmazeid]}                        (:body parameters)
        {:strs [id name summary premiered image]} (tv/show tvmazeid)
        record {:tvmazeid id
                :name     name
                :summary  summary
                :bookmark (-> (f/parse (or premiered "1900-02-01"))
                              (t/minus (t/days 1)))
                :imageurl (get image "medium")}]
    (db/insert! :show record)
    record))

(def routes
  [""
   [
    ["/shows"    (yada/resource
                   {:methods
                    {:get
                     {:produces "application/json"
                      :response shows}}})]
    ["/show-search" (yada/resource
                      {:methods
                       {:get
                        {:parameters {:query {:query String}}
                         :produces "application/json"
                         :response show-search}}})]
    ["/add-show" (yada/resource
                   {:methods
                    {:put
                     {:parameters {:body {:tvmazeid Long}}
                      :consumes   "application/json"
                      :produces   "application/json"
                      :response   add-show!}}})]
    ]])

(defn start-server!
  "Starts a server on the provided `port`.

   Returns a map that includes:

     :port    the port being used
     :close   a function that will stop the server
     :server  a reference to the server"
  [port]
  (yada/listener routes {:port port}))
