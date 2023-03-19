(ns sse.events
  (:require [re-frame.core :as rf]
            [ajax.core :as ajax]
            [sse.subs :refer [create-initial-state]]
            [ajax.edn :refer [edn-response-format edn-request-format]]))

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
 :start-counting-success
 (fn [db [_ response]]
   (assoc db :couting-success response)))

(rf/reg-event-db
 :start-counting-error
 (fn [db [_ response]]
   (assoc db :counting-error response)))

(defn start-counting-handler [cofx _]
  {:db         (assoc (:db cofx) :counting true)
   :fx         (or (:fx cofx) [])
   :http-xhrio {:method          :get
                :uri             "/sse-notif"
                :timeout         8000                   ;; optional see API docs
                :response-format (edn-response-format)  ;; IMPORTANT!: You must provide this.
                :on-success      [:start-counting-success]
                :on-failure      [:start-counting-error]}})

(rf/reg-event-fx
 :start-couting
 start-counting-handler)

(defn >start-counting []
  (rf/dispatch [:start-couting]))