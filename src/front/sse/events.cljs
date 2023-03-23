(ns sse.events
  (:require [re-frame.core :as rf]
            [cljs.reader :as cr]
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
   (js/console.log event)
   (update db :counter-value conj data)))

(defn >start-counting []
  (rf/dispatch [::o/sse-client {:id ::counter-events
                                :uri "/sse-counter"
                                :on-event [::on-count-tick]}]))

(defn >stop-counting []
  (rf/dispatch [::o/abort ::counter-events]))


;; Async Jobs ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; ------------------------------------- ::on-notif-job

(defn on-notif-job-handler
  "Handles job notification channel events"
  [db [_ {:keys [event data]}]]
   ;; 'channel-id' is the first event received by the channel. It provided
   ;; channel id value that must be used when submiting a job
  (if (= "channel-id" event)
    (assoc db :notif-job-channel-id data)
    (let [{:keys [job-id info]} (cljs.reader/read-string data)]
      (case event
        "notif-job-start"    (update db job-id merge info  {:status :started})
        "notif-job-progress" (update db job-id merge info)
        "notif-job-success"  (update db job-id merge info  {:status :success})
        "notif-job-error"    (update db job-id merge info) {:status :error}))))

(rf/reg-event-db
 ::on-notif-job
 on-notif-job-handler)

;; ------------------------------------- :close-notif-job-channel

(defn close-notif-job-channel-handler
  "Handle event fired to close the Job notification channel"
  [cofx  _]
  (-> cofx
      (assoc-in [:db :notif-job-channel-id] nil)
      (update :fx conj [:dispatch [::o/abort ::notif-job-events]])))

(rf/reg-event-fx
 :close-notif-job-channel
 close-notif-job-channel-handler)

(defn >close-notif-job-channel []
  (rf/dispatch [:close-notif-job-channel]))

;; -------------------------------------  :open-notif-job-channel

(defn open-notif-job-channel-handler
  "Hanldes event fired to open a new Job Notification Channel"
  [cofx _]
  (update cofx :fx conj [:dispatch [::o/sse-client {:id       ::notif-job-events
                                                    :uri      "/sse-notif-job"
                                                    :on-event [::on-notif-job]}]]))
(rf/reg-event-fx
 :open-notif-job-channel
 open-notif-job-channel-handler)

(defn >open-notif-job-channel []
  (rf/dispatch [:open-notif-job-channel]))

;; -------------------------------------  :submit-job-success

(defn submit-job-success-handler
  "Handles event received when the job was successfully submited to the server.
   In this case, the Job id is returned in the response"
  [db [_ tmp-job-id response]]
  (let [job {:status :pending
             :params (get-in db [tmp-job-id :params])}]
    (-> db
        (dissoc tmp-job-id)
        (assoc (:job-id response) job))))

(rf/reg-event-db
 :submit-job-success
 submit-job-success-handler)

;; -------------------------------------  :submit-job-error

(defn submit-job-error-handler
  "Handles the event fired when the job could not be successfully submited to the server.
   The response contains the error info"
  [db [_ tmp-job-id response]]
  (update db tmp-job-id #(-> %
                             (assoc :status :error)
                             (assoc :error  response)
                             (dissoc :result))))

(rf/reg-event-db
 :submit-job-error
 submit-job-error-handler)

;; -------------------------------------  :submit-job

(defn submit-job-handler
  "Hanldes event fired by the user submiting a job to the server."
  [cofx [_ job-params]]
  (let [db         (:db cofx)
        channel-id (:notif-job-channel-id db)
        tmp-job-id (str "tmp." (random-uuid))]

    (if channel-id
      {:db (assoc db tmp-job-id  {:status :submited
                                  :params job-params})
       :fx (:fx cofx)
       :http-xhrio {:method          :post
                    :uri             (str "/job/" channel-id)
                    :format          (edn-request-format)
                    :params          job-params
                    :timeout         8000
                    :response-format (edn-response-format)
                    :on-success      [:submit-job-success tmp-job-id]
                    :on-failure      [:submit-job-error   tmp-job-id]}}

      (update cofx :fx conj [:dispatch [:submit-job-error tmp-job-id "no channel"]]))))

(rf/reg-event-fx
 :submit-job
 submit-job-handler)

(defn >submit-job [job-params]
  (rf/dispatch [:submit-job job-params]))
