(ns sse.events
  (:require [re-frame.core :as rf]
            [ajax.core :as ajax]
            [sse.subs :refer [create-initial-state]]
            [ajax.edn :refer [edn-response-format edn-request-format]]
            [oxbow.re-frame :as o]))

(defn initialize-state-handler [_ _]
  (create-initial-state))

(rf/reg-event-db
 :initialize-state
 initialize-state-handler)

(defn >initialize-state
  "Synchronously dispatch the event to initialize the app state"
  []
  (rf/dispatch-sync [:initialize-state]))

;; -------------------------------------------

(rf/reg-event-db
 ::on-count-tick
 (fn [db [_ {:keys [data] :as event}]]
   #_(js/console.log data) 
   (update db :counter-value conj data)))

(defn >start-counting []
  (rf/dispatch [::o/sse-client {:id ::counter-events
                                :uri "/sse-notif"
                                :on-event [::on-count-tick]}]))

(defn >stop-counting []
  (rf/dispatch [::o/abort ::counter-events]))