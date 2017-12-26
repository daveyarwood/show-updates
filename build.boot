(set-env!
  :source-paths   #{"src/backend" "src/frontend" "dev"}
  :resource-paths #{"resources"}
  :dependencies   '[[adzerk/boot-cljs          "2.1.4"     :scope "test"]
                    [adzerk/boot-cljs-repl     "0.3.3"     :scope "test"]
                    [adzerk/boot-reload        "0.5.2"     :scope "test"]
                    [clj-http                  "3.7.0"                  ]
                    [com.cemerick/piggieback   "0.2.2"     :scope "test"]
                    [day8.re-frame/http-fx     "0.1.4"                  ]
                    [migratus                  "1.0.2"                  ]
                    [org.clojure/clojure       "1.9.0"                  ]
                    [org.clojure/clojurescript "1.9.946"                ]
                    [org.clojure/tools.nrepl   "0.2.13"    :scope "test"]
                    [org.slf4j/slf4j-log4j12   "1.8.0-beta0"            ]
                    [org.xerial/sqlite-jdbc    "3.21.0.1"               ]
                    [pandeiro/boot-http        "0.8.3"     :scope "test"]
                    [reagent                   "0.8.0-alpha2"           ]
                    [re-frame                  "0.10.3-beta1"           ]
                    [weasel                    "0.7.0"     :scope "test"]
                    [yada                      "1.2.9"                  ]])

(require
  '[adzerk.boot-cljs      :refer (cljs)]
  '[adzerk.boot-cljs-repl :refer (cljs-repl start-repl)]
  '[adzerk.boot-reload    :refer (reload)]
  '[pandeiro.boot-http    :refer (serve)]
  '[show-updates.api      :as    api]
  '[show-updates.database :as    db]
  '[show-updates.migrate  :as    migrate])

(deftask migrate
  [f db-file DBFILE str "The SQLite database file. (Will be created if it doesn't exist.)"]
  (migrate/migrate! db-file))

(deftask initialize-db
  [f db-file DBFILE str "The SQLite database file. (Will be created if it doesn't exist.)"]
  (with-pass-thru _
    (migrate/migrate! db-file)
    (db/set-db-file! db-file)))

(deftask build
  "This task contains all the necessary steps to produce a build
   You can use 'profile-tasks' like `production` and `development`
   to change parameters (like optimizations level of the cljs compiler)"
  []
  (comp (speak)
        ,,,
        (cljs)))

(deftask serve-backend
  "Serves the backend."
  [f db-file DBFILE str "The SQLite database file to use. (Will be created if it doesn't exist.)"]
  (comp
    (initialize-db :db-file db-file)
    (with-pass-thru _
      (api/start-server! 12345))))

(deftask run
  "The `run` task wraps the building of your application in some
   useful tools for local development: an http server, a file watcher
   a ClojureScript REPL and a hot reloading mechanism"
  []
  (comp (serve)
        (serve-backend :db-file "shows.db")
        (watch)
        (cljs-repl)
        ,,,
        (reload)
        (build)))

(deftask production []
  (task-options! cljs {:optimizations :advanced})
  identity)

(deftask development []
  (task-options! cljs   {:optimizations :none}
                 reload {:on-jsload 'show-updates.app/init})
  identity)

(deftask dev
  "Simple alias to run application in development mode"
  []
  (comp (development)
        (run)))


