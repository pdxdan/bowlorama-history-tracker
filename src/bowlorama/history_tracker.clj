(ns bowlorama.history-tracker
  (:require [amazonica.aws.dynamodbv2 :as ddb]))

(def ^:dynamic btable "bowlorama")

(defn init-bowlorama-table
  "Creates bowlorama table and schema" []
  (ddb/create-table :table-name btable
                :key-schema [{:attribute-name "gameid" :key-type "HASH"}
                             {:attribute-name "player" :key-type "RANGE"}]
                :attribute-definitions [{:attribute-name "gameid" :attribute-type "N"}
                                        {:attribute-name "player" :attribute-type "S"}]
                :provisioned-throughput {:read-capacity-units 1
                                         :write-capacity-units 1}))

(defn player-history
  "Retrieve a Player's ball history"
  [gameid player]
  (:ballhistory (:item (ddb/get-item :table-name btable :key {:gameid gameid :player player}))))

(defn updated-history
  "Lookup a player's ball history and append the latest new ball"
  [gameid player ball]
  (vec (conj (player-history gameid player) ball)))

(defn append-ball-to-history
  "Receive a Player's name and new ball value, then updates their history in persistance"
  [gameid player ball]
  (ddb/put-item
    :table-name btable
    :return-consumed-capacity "TOTAL"
    :return-item-collection-metrics "SIZE"
    :item {:gameid gameid
           :player player
           :ballhistory (updated-history gameid player ball)}))

(defn reset-player-history
  "Receive a Player's name and new ball value, then updates their history in persistance"
  [gameid player]
  (ddb/put-item
    :table-name btable
    :return-consumed-capacity "TOTAL"
    :return-item-collection-metrics "SIZE"
    :item {:gameid gameid
           :player player
           :ballhistory [] }))
