(ns route-test
  (:require [server :as srv]
            [route :refer [default-routes]]
            [clojure.test :refer :all]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as http.routes]
            [io.pedestal.test :refer [response-for]]))

;; helper functions -------------------------------------------------------------

;; Use this function is test so to refer to a route by its name better than
;; by its path
(def url-for (http.routes/url-for-routes (http.routes/expand-routes default-routes)))

(defn content-type [response]
  (get-in response [:headers "Content-Type"]))

;; tests -------------------------------------------------------------------------

(def service (:io.pedestal.http/service-fn (http/create-servlet srv/service-map)))

(deftest default-routes-test

  (testing "route /"
    (let [response (response-for service :get (url-for :home))] ;; could also use "/" instead of url-for
      (is (= 200 (:status response)))
      (is (= "text/html" (content-type response)))
      (is (not-empty (:body response)))))

  (testing "route /about"
    (let [response (response-for service :get "/about")]
      (is (= 200 (:status response)))
      (is (= "text/plain" (content-type response)))))

  (testing "when /about Accept only JSON"
    (let [response (response-for service :get "/about" :headers {"accept" "application/json"})]
      (is (= 200 (:status response)))
      (is (= "application/json" (content-type response)))))
  
  (testing "route /greet with no param"
    (let [response (response-for service :get (url-for :get-greet))]
      (is (= 200 (:status response)))
      (is (= "text/plain" (content-type response)))
      (is (= "{:reply \"hello, stranger\"}" (:body response)))
      ))
  
  (testing "route /greet with unkown name query param"
    (let [response (response-for service :get (url-for :get-greet :query-params {:name "Alice"}))]
      (is (= 200 (:status response)))
      (is (= "text/plain" (content-type response)))
      (is (= "{:reply \"hello, Alice\"}" (:body response)))))
  
  (testing "route /greet with kown name query param"
    (let [response (response-for service :get (url-for :get-greet :query-params {:name "bob"}))]
      (is (= 200 (:status response)))
      (is (= "text/plain" (content-type response)))
      (is (= "{:reply \"Hi bob! I know you\"}" (:body response)))))
  
  )




