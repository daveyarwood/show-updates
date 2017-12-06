(ns show-updates.migrate
  "Database migration setup."
  (:require [migratus.core :as migratus]))

(defn db-config
  [db-file]
  {:store                :database
   :migration-dir        "migrations/"
   :migration-table-name "migrations"
   :db                   {:classname   "org.sqlite.JDBC"
                          :subprotocol "sqlite"
                          :subname     db-file}})

(defn migrate!
  [db-file]
  (migratus/migrate (db-config db-file)))
