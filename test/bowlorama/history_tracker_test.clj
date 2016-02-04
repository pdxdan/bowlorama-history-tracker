(ns bowlorama.history-tracker-test
  (:require [clojure.test :refer :all]
            ;[me.raynes.conch.low-level :as conch]
            [amazonica.aws.dynamodbv2 :as ddb]
            [bowlorama.history-tracker :refer :all]))

(def testtablename "bowloramatest")

;(def local-client-opts
;  "Minimum required connectivity parameters for local in-memory DB"
;  {:access-key "LocalDBAccessDoesNotNeedARealAccessKey"
;   :secret-key "LocalDBAccessDoesNotNeedARealSecretKey"
;   :endpoint "http://localhost:8000"})


;(defn start-local-db
;  "Start the in memory dyanamodb-local process" []
;  (conch/proc "pkill" "-f" "dynamodb-local")
;  (conch/proc "/usr/local/bin/dynamodb-local" "-inMemory")
;  (println "starting dynamodb-local")
;  (Thread/sleep 1000))  ; add 1 second pause. Yeah, I know this is terrible but it is a short term workaround until I figure out how to wait for the DB to start

;(defn teardown-local-db
;  "Stop the in memory dyanamodb-local processes" []
;  (println "executing teardown")
;  (conch/proc "pkill" "-f" "dynamodb-local"))

;(defn onetime-fixture
;  "Start the in-memory DB once for the entire namespace" [f]
;  (start-local-db)
;  (f)
;  (teardown-local-db))

;(defn reset-table
  ;"Drops, and recreates the test table" []
  ;(binding [btable testtablename]
  ;  (ddb/delete-table :table-name testtablename)
  ;  (init-bowlorama-table)
    ;))
;(reset-table)

;(defn foreach-fixture
;  "Reset the DB once for each test fixure" [f]
;  (f)
;  )

;(use-fixtures :once onetime-fixture)
;(use-fixtures :each foreach-fixture)

(defn first3
  "Establish three rounds of bowling history" []
  (binding [btable testtablename]
    (reset-player-history 42 "Donald")
    (reset-player-history 42 "Bernie")
    (append-ball-to-history 42 "Donald" 1)
    (append-ball-to-history 42 "Bernie" 4)
    (append-ball-to-history 42 "Donald" 2)
    (append-ball-to-history 42 "Bernie" 5)
    (append-ball-to-history 42 "Donald" 3)
    (append-ball-to-history 42 "Bernie" 6)))

;(deftest db-schema-tests
;  (testing "The bowlorama table exists"
;    (is (= (.contains (far/list-tables local-client-opts) :bowlorama) true)))
;  (testing "bowlorama table schema supports of composite game/player key"
;    (is (= (:prim-keys (far/describe-table local-client-opts :bowlorama))
;           {:gameid {:key-type :hash, :data-type :n}, :player {:key-type :range, :data-type :s}})))
;  (testing "we can set and retrieve ball history uniquely by game and player"
;    (first3)
;    (far/put-item local-client-opts :bowlorama {:gameid 43 :player "Donald" :ballhistory  [3 0 3] })
;    (is (= (:ballhistory (far/get-item local-client-opts :bowlorama {:gameid 42 :player "Donald"}))
;           [1 2 3]))
;    (is (= (:ballhistory (far/get-item local-client-opts :bowlorama {:gameid 43 :player "Donald"}))
;           [3 0 3]))))


(deftest history-maintenance
  (binding [btable testtablename]                  ; override client-opts with local definition
    (testing "Retrieve player history from the DB"
      (first3)
      (is (= (player-history 42 "Bernie") [4 5 6])))
    (testing "Producing an updated ball history"
      (first3)
      (is (= (updated-history 42 "Bernie" 3) [4 5 6 3])))
    (testing "Storing updated history in the DB"
      (first3)
      (append-ball-to-history 42 "Bernie" 2)
      (append-ball-to-history 42 "Bernie" 10)
      (is (= (player-history 42 "Bernie") [4 5 6 2 10])))))


(binding [btable testtablename]
   ;(ddb/delete-table :table-name testtablename)
   ;(init-bowlorama-table)
   ;(append-ball-to-history 43 "Ted" 2)
   ;(player-history 42 "Bernie")
   ;(first3)
   ;(reset-player-history 42 "Donald")
   )