(ns core
  (:require [utils :refer [add-one]]
            [default.app :as  default-app]))

;; function 'add-one' is defined as a shared function
(defn some-func [i]
  (add-one i))

(default-app/render "root")

;;  Lifecycle Hooks =================================

(defn ^:dev/before-load stop []
  (js/console.log "stop"))

(defn ^:dev/after-load start []
  (js/console.log "start"))

(defn ^:dev/before-load-async async-stop [done]
  (js/console.log "stop")
  (js/setTimeout
   (fn []
     (js/console.log "stop complete")
     (done))))

(defn ^:dev/after-load-async async-start [done]
  (js/console.log "start")
  (js/setTimeout
   (fn []
     (js/console.log "start complete")
     (done))))