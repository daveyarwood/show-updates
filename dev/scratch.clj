(ns scratch
  (:require [show-updates.api      :as api]
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
  (db/insert! :show {:name "The Walking Dead" :tvmazeid 73})
  (db/insert! :show {:name "Gilmore Girls" :tvmazeid 525}))

(comment
  (init!)
  (seed-db!)
  (db/query "SELECT * FROM show")
  (tv/show-search "Gilmore Girls")
  (api/shows {})
  (api/add-show! {:parameters {:body {:tvmazeid 12345}}})
)
