(defproject bowlorama-history-tracker "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 ;[com.taoensso/faraday "1.9.0-beta1"]
                 ;[me.raynes/conch "0.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [uswitch/lambada "0.1.0"]
                 [amazonica "0.3.49"]
                 ;[clj-http "2.0.1"]
                 [clj-http "3.0.0-SNAPSHOT"]
                 [cheshire "5.5.0"]
                 ]
  :profiles {:uberjar {:aot :all}}
  :uberjar-name "bowlorama-history-tracker.jar")
