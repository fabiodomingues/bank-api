(ns bank-api.logic)

(defn valid-cpf? [cpf]
  (re-matches #"\d{3}\.\d{3}\.\d{3}\-\d{2}" cpf))

(defn new-account
  [{:keys [name cpf]}]
  (if (valid-cpf? cpf)
    {:id (random-uuid)
     :name name
     :cpf cpf
     :balance 0M}
    (throw (Exception. "Invalid cpf"))))

(defn deposit
  [account
   amount]
  (update account :balance + amount))

(defn withdrawal
  [account
   amount]
  (update account :balance - amount))