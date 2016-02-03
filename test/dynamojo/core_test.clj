(ns dynamojo.core-test
  (:require [clojure.test :refer :all]
            [dynamojo.core :refer :all]
            [me.raynes.conch.low-level :as conch]
            [taoensso.faraday :as far]))

(def client-opts
  "Minimum required connectivity parameters for local in-memory DB"
  {:access-key "LocalDBAccessDoesNotNeedARealAccessKey"
   :secret-key "LocalDBAccessDoesNotNeedARealSecretKey"
   :endpoint "http://localhost:8000"})

(defn start-local-db
  "Start the in memory dyanamodb-local process" []
  (conch/proc "pkill" "-f" "dynamodb-local")
  (conch/proc "/usr/local/bin/dynamodb-local" "-inMemory")
  (println "starting dynamodb-local")
  (Thread/sleep 1000))  ; add 1 second pause. Yeah, I know this is terrible but it is a short term workaround until I figure out how to wait for the DB to start

(defn teardown-local-db
  "Stop the in memory dyanamodb-local processes" []
  (println "executing teardown")
  (conch/proc "pkill" "-f" "dynamodb-local"))

(defn onetime-fixture
  "Start the in-memory DB once for the entire namespace" [f]
  (start-local-db)
  (f)
  (teardown-local-db))

(defn foreach-fixture
  "Reset the DB once for each test fixure" [f]
  (init-bowlorama-table client-opts)
  (f)
  (far/delete-table client-opts :bowlorama))

(use-fixtures :once onetime-fixture)
(use-fixtures :each foreach-fixture)

(defn first3
  "Establish three rounds of bowling history" []
  (far/put-item client-opts :bowlorama {:gameid 1 :player "Donald" :ballhistory  [1 2 3] })
  (far/put-item client-opts :bowlorama {:gameid 1 :player "Bernie" :ballhistory  [4 5 6] }))

(deftest db-schema-tests
  (testing "The bowlorama table exists"
    (is (= (.contains (far/list-tables client-opts) :bowlorama) true)))
  (testing "bowlorama table schema supports of composite game/player key"
    (is (= (:prim-keys (far/describe-table client-opts :bowlorama))
           {:gameid {:key-type :hash, :data-type :n}, :player {:key-type :range, :data-type :s}})))
  (testing "we can set and retrieve ball history uniquely by game and player"
    (first3)
    (far/put-item client-opts :bowlorama {:gameid 2 :player "Donald" :ballhistory  [3 0 3] })
    (is (= (:ballhistory (far/get-item client-opts :bowlorama {:gameid 1 :player "Donald"}))
           [1 2 3]))
    (is (= (:ballhistory (far/get-item client-opts :bowlorama {:gameid 2 :player "Donald"}))
           [3 0 3]))))

(deftest history-maintenance
  (testing "Producing an updated ball history"
    (first3)
    (is (= (updated-history client-opts "Bernie" 3)
           [4 5 6 3])))
  (testing "Storing and retrieving updated history in the DB"
    (first3)
    (append-ball-to-history client-opts "Bernie" 2)
    (append-ball-to-history client-opts "Bernie" 10)
    (is (=  (:ballhistory (far/get-item client-opts :bowlorama {:gameid 1 :player "Bernie"}))
            [4 5 6 2 10]))))


;(def ^:dynamic client-opts
;  (let-programs [aws "/usr/local/bin/aws"]
;                {:access-key (aws "configure" "get" "aws_access_key_id")
;                 :secret-key (aws "configure" "get" "aws_secret_access_key")
;                 :endpoint "http://localhost:8000"
;                 ;:endpoint "http://dynamodb.us-east-1.amazonaws.com" ; Virginia
;                 }
;                )
;  )

;(def client-opts
;  {;;; For DDB Local just use some random strings here, otherwise include your IAM keys:
;   :access-key "AKIAJKEZHOAZL4XXKJSA"
;   :secret-key "KE7f9tsg6ySaglFB09CEqz0TI1qWc6u6f+QyRpAG"
;
;   ;;; You may optionally override the default endpoint if you'd like to use DDB or a different region
;   :endpoint "http://localhost:8000"                   ; For DDB Local
;   ; :endpoint "http://dynamodb.us-west-2.amazonaws.com" ; Oregon
;   ; :endpoint "http://dynamodb.us-east-1.amazonaws.com" ; Virginia
;   })

