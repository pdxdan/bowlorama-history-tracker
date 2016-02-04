(ns bowlorama.lambda
  (:require [uswitch.lambada.core :refer [deflambdafn]]
            [clojure.data.json :as json]
            [bowlorama.history-tracker :as bhistory]
            [clojure.java.io :as io]))

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

(defn handle-append-ball-to-history-event
  [event]
  (println "Got the following request: " (pr-str event))
  (let [gameid (get event "gameid")]
    (let [player (get event "player")]
      (bhistory/append-ball-to-history gameid player (get event "ball"))
      {:status "ok",
       :ballhistory (bhistory/player-history gameid player)})))


(deflambdafn bowlorama.lambda.append-ball-to-history
             [in out ctx]
             (let [event (json/read (io/reader in))
                   res (handle-append-ball-to-history-event event)]
               (with-open [w (io/writer out)]
                 (json/write res w))))

