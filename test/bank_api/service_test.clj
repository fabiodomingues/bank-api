(ns bank-api.service-test
  (:require [clojure.test :refer :all]
            [io.pedestal.test :refer :all]
            [io.pedestal.http :as bootstrap]
            [bank-api.service :as service]
            [bank-api.logic :as logic]))

(def service
  (::bootstrap/service-fn (bootstrap/create-servlet service/service)))

(deftest home-page-test
  (is (=
       (:body (response-for service :get "/"))
       "Hello World!"))
  (is (=
       (:headers (response-for service :get "/"))
       {"Content-Type" "text/html;charset=UTF-8"
        "Strict-Transport-Security" "max-age=31536000; includeSubdomains"
        "X-Frame-Options" "DENY"
        "X-Content-Type-Options" "nosniff"
        "X-XSS-Protection" "1; mode=block"
        "X-Download-Options" "noopen"
        "X-Permitted-Cross-Domain-Policies" "none"
        "Content-Security-Policy" "object-src 'none'; script-src 'unsafe-inline' 'unsafe-eval' 'strict-dynamic' https: http:;"})))

(deftest about-page-test
  (is
   (re-find #"Clojure \d+\.\d+(\.\d+)?"
            (:body (response-for service :get "/about"))))
  
  (is (=
       (:headers (response-for service :get "/about"))
       {"Content-Type" "text/html;charset=UTF-8"
        "Strict-Transport-Security" "max-age=31536000; includeSubdomains"
        "X-Frame-Options" "DENY"
        "X-Content-Type-Options" "nosniff"
        "X-XSS-Protection" "1; mode=block"
        "X-Download-Options" "noopen"
        "X-Permitted-Cross-Domain-Policies" "none"
        "Content-Security-Policy" "object-src 'none'; script-src 'unsafe-inline' 'unsafe-eval' 'strict-dynamic' https: http:;"})))

(deftest is-valid-cpf-test
  (is (false? (service/valid-cpf? "12345678910"))) 
  (is  (false? (service/valid-cpf? "")))
  (is  (false? (service/valid-cpf? "xyz.456.789-10")))
  (is  (true? (service/valid-cpf? "123.456.789-10")))
   )

(deftest new-valid-account-test
  (let [new-account (logic/new-account {:name "Jade" :cpf "123.456.789-10"})]
    (is (uuid? (:id new-account)))
    (is (= "Jade" (:name new-account)))
    (is (= "123.456.789-10" (:cpf new-account)))
    (is (= 0M (:balance new-account))) 
    ))

(deftest new-valid-account-without-cpf-test
  (let [new-account (logic/new-account {:name "Jade"})]
    (is (uuid? (:id new-account)))
    (is (= "Jade" (:name new-account)))
    (is (nil? (:cpf new-account)))
    (is (= 0M (:balance new-account)))))


(deftest new-invalid-account-test
  (is (thrown? Exception (logic/new-account {:name "Jade" :cpf "1"}))))


(deftest deposit-test
  (let [account (logic/new-account {:name "Jade"})]
    (is (= (logic/deposit account 10) (update account :balance + 10)))
    (is (= (logic/deposit account 0) account))
    (is (= (logic/deposit account 11.93) (update account :balance + 11.93)))
))

(deftest withdrawal-test
  (let [account (logic/new-account {:name "Jade"})]
    (is (= (logic/withdrawal account 10) (update account :balance - 10)))
    (is (= (logic/withdrawal account 0) account))
    (is (= (logic/withdrawal account 11.93) (update account :balance - 11.93)))))