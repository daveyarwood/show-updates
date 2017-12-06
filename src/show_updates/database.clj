(ns show-updates.database
  (:require [clojure.java.jdbc :as sql]))

(def ^:dynamic *db* nil)

(defn db-config
  [db-file]
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     db-file})

(defn set-db-file!
  [db-file]
  (alter-var-root #'*db* (constantly (db-config db-file))))

(defn query
  [& args]
  (apply sql/query *db* args))

(defn execute!
  [& args]
  (apply sql/execute! *db* args))

(defn insert!
  [& args]
  (apply sql/insert! *db* args))
