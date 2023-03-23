(ns sse.subs
  (:require [re-frame.core :as rf]))

(def db-shape {:counting  true
               :counter-value 2

               ;; SSE event channel identifier or nil
               :notif-job-channel-id ""
               :submited-job {:param1 1}
               :submited-job-error {}
               "jobid" {;; :submited, :pending :started :success :error
                        :status :submited
                        :params {:param1 1}
                        :error {}
                        :result {}}})

(defn create-initial-state []
  (merge {:counting false
          :counter-value nil
          :notif-job-channel-id nil}))

;; layer 2 ------------------------------------------------------

(rf/reg-sub  :counter-value
             (fn [db _]
               (:counter-value db)))

(defn <counter-value []
  @(rf/subscribe [:counter-value]))

(rf/reg-sub :notif-job-channel-id
            (fn [db _]
              (:notif-job-channel-id db)))

(defn <notif-job-channel-id []
  @(rf/subscribe [:notif-job-channel-id]))