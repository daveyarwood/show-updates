(set-env!
  :source-paths   #{"src" "dev"}
  :resource-paths #{"resources"}
  :dependencies   '[[clj-http                "3.7.0"]
                    [migratus                "1.0.1"]
                    [org.clojure/clojure     "1.9.0-RC2"]
                    [org.slf4j/slf4j-log4j12 "1.7.9"]
                    [org.xerial/sqlite-jdbc  "3.8.6"]
                    [yada                    "1.2.9"]])

(require '[show-updates.migrate :as migrate])

(deftask migrate
  [f db-file DBFILE str "The SQLite database file. (Will be created if it doesn't exist.)"]
  (migrate/migrate! db-file))
