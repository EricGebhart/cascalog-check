(defproject cascalog-check "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                                        ;[org.clojure/clojure "1.8.0"]
                 [cascalog/cascalog-core "3.0.0"
                  :exclude [[prismatic/schema "0.3.7"]
                                        ;prismatic/schema has [potempkin "0.3.2"]
                                        ; this is bad for eastwood.
                            ]]
                 ;;[potemkin "0.4.3"]
                 [prismatic/schema "1.1.2"] ;; has potemkin 0.4.1

                 ]

  :plugins [[lein-environ "1.0.0"]
            [lein-typed "0.3.5"]]

  :core.typed {:check [cascalog-check.core]}

  :profiles {:provided {:dependencies [[org.apache.hadoop/hadoop-core "1.2.1"]]}

             :dev {:source-paths ["dev" "env/dev/clj"]
                   :test-paths ["test"]
                   :resource-paths ["resources" "resources/schemas"]


                   :dependencies [[midje "1.8.3"]
                                  [org.apache.hadoop/hadoop-core "1.2.1"]]

                   :env {:squiggly {:checkers [:kibit] ;; :eastwood
                                    :eastwood-exclude-linters [:unlimited-use]
                                    :eastwoood-options {;; :bultin-config-files ["myconfigfile.clj"]
                                                        }}}}})
