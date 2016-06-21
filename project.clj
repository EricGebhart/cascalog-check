(defproject cascalog-check "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                                        ;[org.clojure/clojure "1.8.0"]
                 [cascalog/cascalog-core "3.0.0"]]

  :plugins [[lein-environ "1.0.0"]
            [lein-typed "0.3.5"]]

  :core.typed {:check [cascalog-check.core]}

  :profiles {:provided {:dependencies [[org.apache.hadoop/hadoop-core "1.2.1"]]}

             :dev {:source-paths ["dev" "env/dev/clj"]
                   :test-paths ["test"]
                   :resource-paths ["resources" "resources/schemas"]


                   :dependencies [[midje "1.8.3"]
                                  [org.apache.hadoop/hadoop-core "1.2.1"]]

                   :env {:squiggly {:checkers [:eastwood :kibit]
                                    :eastwood-exclude-linters [:unlimited-use]
                                    :eastwoood-options {;; :bultin-config-files ["myconfigfile.clj"]
                                                        }}}}})
