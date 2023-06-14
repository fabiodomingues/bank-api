(ns bank-api.account-repository)

(def ^:private db (atom {}))

(defn upsert!
  [{:keys [id] :as account}]
  (swap! db assoc id account))

(defn with-id!
  [id]
  (get @db id))
