(ns route-test
  (:require [server :as srv]
            [route :refer [default-routes]]
            [clojure.test :refer :all]
            [clojure.data.json :as json]
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
      (is (= "{:reply \"hello, stranger\"}" (:body response)))))

  (testing "route /greet with unknown name query param"
    (let [response (response-for service :get (url-for :get-greet :query-params {:name "Alice"}))]
      (is (= 200 (:status response)))
      (is (= "text/plain" (content-type response)))
      (is (= "{:reply \"hello, Alice\"}" (:body response)))))

  (testing "route /greet with known name query param and JSON response"
    (let [response (response-for service
                                 :get (url-for :get-greet :query-params {:name "bob"})
                                 :headers {"Accept"   "application/json"})]
      (is (= 200 (:status response)))
      (is (= "application/json" (content-type response)))
      (is (= {:reply "Hi bob! I know you"} (json/read-str (:body response) :key-fn keyword)))))

  (testing "route POST /greet"
    (let [response (response-for service
                                 :post (url-for :post-greet)
                                 :headers {"Content-Type" "application/json"
                                           "Accept"       "application/json"}
                                 :body (json/write-str {:name "bob"}))]
      (is (= 200 (:status response)))
      (is (= "application/json" (content-type response)))
      (is (=  {:reply "Hi bob! I know you"} (json/read-str (:body response) :key-fn keyword)))))

  (testing "route POST /upload"
    (let [form-body (str "--XXXX\r\n"
                         "Content-Disposition: form-data; name=\"file1\"; filename=\"foobar1.txt\"\r\n\r\n"
                         "bar\r\n"
                         "--XXXX\r\n"
                         "Content-Disposition: form-data; name=\"file2\"; filename=\"foobar2.txt\"\r\n\r\n"
                         "baz\r\n"
                         "--XXXX--")

          response (response-for service
                                 :post    (url-for :post-upload)
                                 :body    form-body
                                 :headers {"Content-Type" "multipart/form-data; boundary=XXXX"
                                           "Accept"       "application/json"})]

      (is (= 200 (:status response)))
      (let [body (:body response)
            body-map (json/read-str  body :key-fn keyword)]
        (is  (:file1 body-map))
        (is  (:file2 body-map))))))
