(ns bowlorama.history-tracker
  (:require [taoensso.faraday :as far]
            [me.raynes.conch :refer [programs with-programs let-programs] :as sh]))

(defn init-bowlorama-table
  "Creates bowlorama table and schemae"
  [client-opts]
  (far/create-table client-opts :bowlorama
                    [:gameid :n]                            ; Partition (primary) key named "gameid" of number type
                    {:range-keydef [:player :s]             ; Sort (range) key definition
                     :throughput {:read 1 :write 1}         ; Read & write capacity (units/sec)
                     :block? true                           ; Block thread during table creation
                     }))

(defn player-history
  "Retrieve a Player's ball history"
  [client-opts gameid player]
  (:ballhistory (far/get-item client-opts :bowlorama {:gameid gameid :player player}))
  )

(defn updated-history
  "Receive a Player's name and new ball value and produces the new ball history"
  [client-opts gameid player ball]
  (conj (:ballhistory (far/get-item client-opts :bowlorama {:gameid gameid :player player}))
        ball))

(defn append-ball-to-history
  "Receive a Player's name and new ball value, then updates their history in persistance"
  [client-opts gameid player ball]
  (far/put-item client-opts
                :bowlorama
                {:gameid      gameid ; Remember that this is our primary (indexed) key
                 :player      player
                 :ballhistory (updated-history client-opts gameid player ball)}))


