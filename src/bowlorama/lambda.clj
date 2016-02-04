(ns bowlorama.lambda
  (:require [uswitch.lambada.core :refer [deflambdafn]]
            [clojure.data.json :as json]
            [bowlorama.history-tracker :as bhistory]
            [clojure.string :as str]
            [clojure.java.io :as io]))

(defn parse-int
  "More robust conversion of strings to integers"
  [s]
  (Integer/parseInt (re-find #"\A-?\d+" s)))


(defn handle-player-history-event
  [event]
  (println "Got the following event: " (pr-str event))
  (let [gameid (get event "gameid")]
    (let [player (get event "player")]
      {:status "ok",
       :gameid gameid,
       :player player,
       :ballhistory (bhistory/player-history gameid player)}
    ))
  )

(deflambdafn bowlorama.lambda.player-history
             [in out ctx]
             (let [event (json/read (io/reader in))
                   res (handle-player-history-event event)]
               (with-open [w (io/writer out)]
                 (json/write res w))))

