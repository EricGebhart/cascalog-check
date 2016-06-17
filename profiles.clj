{:user {:signing {:gpg-key "7D97E976"}

        :datomic {:install-location "resources/datomic/datomic-pro"}

        :plugins [;;[refactor-nrepl "2.3.0-SNAPSHOT"]
                  ;;[cider/cider-nrepl "0.13.0-SNAPSHOT"]
                  [lein-datomic "0.2.0"]
                  [lein-try "0.4.3"]
                  [lein-midje "3.1.3"]
                  [chestnut/lein-template "0.7.0" :exclusions [org.clojure/core.cache]]
                  [lein-ancient "0.6.10"]
                  [lein-bikeshed "0.2.0"]
                  [jonase/eastwood "0.2.3" :exclusions [org.clojure/clojure]]
                  [lein-kibit "0.1.2"]
                  [lein-cloverage "1.0.6"]]

        :dependencies [[org.clojure/tools.namespace "0.2.11"]
                       ;[org.clojure/tools.nrepl "0.2.12"]
                       [acyclic/squiggly-clojure "0.1.5"]
                       ^:replace [org.clojure/tools.nrepl "0.2.12"]
                       [org.clojure/core.typed "0.3.23"]
                       [slamhound "1.5.5"]]

        ;; :env {:squiggly {:checkers [:eastwood]
        ;;                  :eastwood-exclude-linters [:unlimited-use]}}


        :aliases {"slamhound" ["run" "-m" "slam.hound"]}}}

{:repl {:plugins [[cider/cider-nrepl "0.12.0"]]}}
