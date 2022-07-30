(ns route
  (:require [clojure.java.io :as io]
            [io.pedestal.http.route :as prt]
            [io.pedestal.http.body-params :refer [body-params]]
            [response :as resp]))

(def common-interceptors [resp/coerce-body resp/content-negotiator (body-params)])

(defn response [status body & {:as headers}]
  {:status status :body body :headers headers})

(def ok       (partial response 200))
(def created  (partial response 201))
(def accepted (partial response 202))

(defn not-found []
  {:status 404 :body "Not found or forbidden name\n"})

;; /echo -------------------------------------------------

(def echo
  {:name ::echo
   :enter #(assoc % :response (ok (:request %))) ;; anonymous function to update the :response
                                                 ;; key in the context map
   })

;; /about -------------------------------------------------

(defn about
  "Request handler returing clojure and Java version "
  [_]
  (resp/ok {:clojure-version (clojure-version)
            :java-version    (System/getProperty "java.version")
            :java-vm-version (System/getProperty "java.vm.version")
            :java-vendor     (System/getProperty "java.vendor")}))

;; / ------------------
(defn home [_]
  (resp/ok (slurp (io/resource "public/index.html")) {"Content-Type" "text/html"}))

;; Routes -------------------------------------------------

(def routes
  (prt/expand-routes
   #{["/"      :get [home] :route-name :home]
     ["/echo"  :get [echo] :route-name :echo]
     ["/about" :get (conj common-interceptors about) :route-name :get-about]}))