(ns bowlorama.history-tracker
  (:require [taoensso.faraday :as far]
            [me.raynes.conch :refer [programs with-programs let-programs] :as sh]))

(def ^:dynamic client-opts
  "Minimum required connectivity parameters for local in-memory DB"
  {:access-key "LocalDBAccessDoesNotNeedARealAccessKey"
   :secret-key "LocalDBAccessDoesNotNeedARealSecretKey"
   })

(defn init-bowlorama-table
  "Creates bowlorama table and schemae"
  [client-opts]
  (far/create-table client-opts :bowlorama
                    [:gameid :n]                            ; Partition (primary) key named "gameid" of number type
                    {:range-keydef [:player :s]             ; Sort (range) key definition
                     :throughput {:read 1 :write 1}         ; Read & write capacity (units/sec)
                     :block? true }))                       ; Block thread during table creation

(defn player-history
  "Retrieve a Player's ball history"
  ;[client-opts gameid player]
  [gameid player]
  (:ballhistory (far/get-item client-opts :bowlorama {:gameid gameid :player player})))

(defn updated-history
  "Lookup a player's ball history and append the latest new ball"
  ;[client-opts gameid player ball]
  [gameid player ball]
  (conj (:ballhistory (far/get-item client-opts :bowlorama {:gameid gameid :player player}))
        ball))

(defn append-ball-to-history
  "Receive a Player's name and new ball value, then updates their history in persistance"
  ;[client-opts gameid player ball]
  [gameid player ball]
  (far/put-item client-opts
                :bowlorama
                {:gameid      gameid
                 :player      player
                 :ballhistory (updated-history gameid player ball)}))
