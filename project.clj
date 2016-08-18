(defproject address-book "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]]
  :plugins [[lein-ring "0.9.7"]
            [lein-cljfmt "0.5.3"]
            [lein-codox "0.9.6"]]
  :ring {:handler address-book.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]
                        [cheshire "5.6.3"]
                        [prismatic/schema "1.1.3"]
                        [org.clojure/core.async "0.2.385"]]}})

