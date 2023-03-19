(ns core
  (:require [utils :refer [add-one]]
            [re-frame.core :as rf]
            [default.app :as  default-app]
            [with-route.app :as routed-app]
            [sse.app :as sse-app]))

;; function 'add-one' is defined as a shared function
(defn some-func [i]
  (add-one i))

(defn run []
  (sse-app/init "root")
  #_(routed-app/init "root")
  #_(routed-app/render "root")
  #_(default-app/render "root")
  )

(def debug? ^boolean goog.DEBUG)

(defn dev-setup []
  (when debug?
    (enable-console-print!)
    (js/console.log "%c -- dev mode --" "background: #2196f3; color: white")))

;;  Lifecycle Hooks =================================

(defn ^:dev/before-load stop []
  (js/console.log "/before-load"))

(defn ^:dev/after-load start []
  (js/console.log "after-load")
  (dev-setup)
  (rf/clear-subscription-cache!)
  (run))

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