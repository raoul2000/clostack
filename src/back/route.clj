(ns route
  "Default Routes"
  (:require [clojure.java.io :as io]
            [io.pedestal.http.route :as prt]
            [io.pedestal.http.body-params :refer [body-params]]
            [response :as resp]))

(def common-interceptors [resp/coerce-body resp/content-negotiator (body-params)])

;; / -----------------------------------------------------

(defn home [_]
  (resp/ok (slurp (io/resource "public/index.html")) {"Content-Type" "text/html"}))

;; /echo -------------------------------------------------

(def echo
  {:name ::echo
   :enter #(assoc % :response (resp/ok (:request %))) ;; anonymous function to update the :response
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

;; GET /greet ---------------------------------------------

(defn greeting-for
  "Returns string to greet a given name"
  [name]
  (cond
    (nil? name)            "hello, stranger"
    (#{"bob" "max"} name)  (str "Hi " name "! I know you")
    :else                  (str "hello, " name)))

(defn greet [request]
  (if-let [greeting (greeting-for (get-in request [:query-params :name]))]
    (resp/ok {:reply greeting})
    (resp/not-found)))

;; Routes -------------------------------------------------

(def default-routes #{["/"      :get [home] :route-name :home]
                      ["/echo"  :get [echo] :route-name :echo]
                      ["/about" :get (conj common-interceptors about) :route-name :get-about]
                      ["/greet" :get (conj common-interceptors greet) :route-name :get-greet]})

(def routes
  (prt/expand-routes default-routes))