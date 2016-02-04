(ns bowlorama.lambda
  (:require [uswitch.lambada.core :refer [deflambdafn]]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clj-http.client :as client]
            [cheshire.core :refer :all]
            [bowlorama.history-tracker :as bhistory]))

(defn handle-player-history-event
  [event]
  (println "Got the following request: " (pr-str event))
  {:ballhistory (bhistory/player-history (get event "gameid") (get event "player"))})

(deflambdafn bowlorama.lambda.player-history
             [in out ctx]
             (let [event (json/read (io/reader in))
                   res (handle-player-history-event event)]
               (with-open [w (io/writer out)]
                 (json/write res w))))

(defn calculated-score
  "Calls the bowlorama-score endpoint and returns a score"
  [rolls]
  (let [score-api "https://c6rt4qktca.execute-api.us-east-1.amazonaws.com/prod/bowlorama-score"]
    (let [reply (client/post score-api {:form-params {:rolls rolls} :content-type :json})]
     (:score (parse-string (:body reply) true)))))

(defn handle-append-ball-to-history-event
  [event]
  (println "Got the following request: " (pr-str event))
  (let [gameid (get event "gameid")]
    (let [player (get event "player")]
      (let [ball (get event "ball")]
        (bhistory/append-ball-to-history gameid player ball)
        (let [ballhistory (bhistory/player-history gameid player)]
          (let [score (calculated-score (clojure.string/join "," ballhistory))]
            {:status "ok",
             :gameid gameid,
             :player player,
             :ball ball
             :ballhistory ballhistory
             :score score }))))))

(deflambdafn bowlorama.lambda.append-ball-to-history
             [in out ctx]
             (let [event (json/read (io/reader in))
                   res (handle-append-ball-to-history-event event)]
               (with-open [w (io/writer out)]
                 (json/write res w))))

