(ns bank-api.service
  (:require [clojure.data.json :as json]
            [io.pedestal.http :as http]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.content-negotiation :as content-negotiation]
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
    {:keys [amount]} :json-params}]
  (-> (account-repository/with-id! (parse-uuid id))
      (logic/deposit amount)
      account-repository/upsert!)
  {:status 204})

(def supported-types ["application/json"
                      "application/edn"
                      "text/plain"
                      "text/html"])

(def content-negotiation-interceptor (content-negotiation/negotiate-content supported-types))

(defn accepted-type
  [context]
  (get-in context [:request :accept :field] "text/plain"))

(defn transform-content
  [body content-type]
  (case content-type
    "text/html"        body
    "text/plain"       body
    "application/edn"  (pr-str body)
    "application/json" (json/write-str body)))

(defn coerce-to
  [response content-type]
  (-> response
      (update :body transform-content content-type)
      (assoc-in [:headers "Content-Type"] content-type)))

(def coerce-body-response
  {:name ::coerce-body-response
   :leave
   (fn [context]
     (if (get-in context [:response :headers "Content-Type"])
       context
       (update-in context [:response] coerce-to (accepted-type context))))})

(def common-interceptors [coerce-body-response
                          content-negotiation-interceptor
                          (body-params/body-params)])

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
