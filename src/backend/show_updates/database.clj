(ns show-updates.database
  "Database helper functions."
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

(defmacro with-transaction
  [& body]
  `(sql/with-db-transaction [conn# *db*]
     (binding [*db* conn#]
       ~@body)))
