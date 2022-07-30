(ns response
  (:require  [io.pedestal.http.content-negotiation :as conneg]
             [clojure.data.json :as json]
             [cognitect.transit :as transit])
  (:import  [java.io ByteArrayInputStream ByteArrayOutputStream]))

;; helper -------------------------------------------------------------

(defn response [status body & {:as headers}]
  {:status status :body body :headers (or headers {})})

(def ok        (partial response 200))
(def created   (partial response 201))
(def accepted  (partial response 202))
(def not-found (partial response 404))


;; ----------------------------------------------------------------------------------

(def supported-types 
  "Vector of supported response MIME types"
  ["text/html" 
   "text/plain"
   "application/edn" 
   "application/transit+json" 
   "application/json"])

(def content-negotiator
  "Content negotiation function for the supported content types"
  (conneg/negotiate-content supported-types))

(defn accepted-type
  [context]
  (get-in context [:request :accept :field] "text/plain"))


(defn ->transit+json 
  "Converts and returns *body* into a transit json formatted string"
  [body]
  (let [out    (ByteArrayOutputStream. 4096)
        writer (transit/writer out :json)]
    (transit/write writer body)
    (.toString out)))

(defn transform-content 
  [body content-type]
  (case content-type
    "text/html"                body
    "text/plain"               body
    "application/edn"          (pr-str body)
    "application/json"         (json/write-str body)
    "application/transit+json" (->transit+json body)))

(defn coerce-to
  [response content-type]
  (-> response
      (update :body transform-content content-type)
      (assoc-in [:headers "Content-Type"] content-type)))

;; interceptor

(def coerce-body
  {:name ::coerce-body
   :leave
   (fn [context]
     (cond-> context
       (nil? (get-in context [:response :headers "Content-Type"]))
       (update-in [:response] coerce-to (accepted-type context))))})
