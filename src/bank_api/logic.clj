(ns bank-api.logic)

(defn new-account
  [{:keys [name]}]
  {:id (random-uuid)
   :name name
   :balance 0M})

(defn deposit
  [account
   {:keys [amount]}]
  (update account :balance + amount))
