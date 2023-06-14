(ns bank-api.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.body-params :as body-params]
            [ring.util.response :as ring-resp]
            [bank-api.account-repository :as account-repository]
            [bank-api.logic :as logic]))

(defn create-account
  [{account-creation-data :json-params}]
  (let [new-account (logic/new-account account-creation-data)]
    (account-repository/upsert! new-account)
    (ring-resp/response new-account)))

(defn account-by-id
  [{{:keys [id]} :path-params}]
  (let [account (account-repository/with-id! (parse-uuid id))]
    (ring-resp/response account)))

(defn deposit
  [{{:keys [id]} :path-params
    deposit-data :json-params}]
  (-> (account-repository/with-id! (parse-uuid id))
      (logic/deposit deposit-data)
      account-repository/upsert!)
  {:status 204})

(def common-interceptors [(body-params/body-params)])

(def routes #{["/accounts" :post (conj common-interceptors `create-account)]
              ["/accounts/:id" :get (conj common-interceptors `account-by-id)]
              ["/accounts/:id/deposits" :post (conj common-interceptors `deposit)]})

(def service {:env :prod
              ::http/routes routes
              ::http/type :jetty
              ::http/port 8080
              ::http/container-options {:h2c? true
                                        :h2? false
                                        :ssl? false}})
