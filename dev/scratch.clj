(ns scratch
  (:require [clj-time.format       :as f]
            [show-updates.api      :as api]
            [show-updates.database :as db]
            [show-updates.migrate  :as migrate]
            [show-updates.tvmaze   :as tv]))

(def ^:const DB-FILE "shows.db")

(defn init!
  []
  (migrate/migrate! DB-FILE)
  (db/set-db-file! DB-FILE))

(defn seed-db!
  "Populates database with test data."
  []
  (db/execute! "DELETE FROM show")
  (doall (for [tvmazeid (repeatedly 10 #(rand-int 1000))]
           (api/add-show! {:parameters {:body {:tvmazeid tvmazeid}}}))))

(comment
  (init!)
  (seed-db!)
  (db/query "SELECT * FROM show")
  (tv/show 525)
  (tv/show 73)
  (tv/show-search "Gilmore Girls")
  (api/shows {})
  (api/add-show! {:parameters {:body {:tvmazeid 12345}}})
)
