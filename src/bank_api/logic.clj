(ns bank-api.logic)

(defn valid-cpf? [cpf]
  (re-matches #"\d{3}\.\d{3}\.\d{3}\-\d{2}" cpf))

(defn new-account
  [{:keys [name cpf]}] 
  (let [account     {:id (random-uuid)
                     :name name
                     :balance 0M}]
    (if (nil? cpf) account
        (if (valid-cpf? cpf) (assoc account :cpf cpf)
            (throw (Exception. "Invalid cpf"))))))

(defn deposit
  [account
   amount]
  (update account :balance + amount))

(defn withdrawal
  [account
   amount]
  (update account :balance - amount))