(ns sse.subs
  (:require [re-frame.core :as rf]))

(defn create-initial-state []
  (merge {:counting false
          :counter-value nil}))

;; layer 2 ------------------------------------------------------

(rf/reg-sub  :counter-value
             (fn [db _]
               (:counter-value db)))

(defn <counter-value []
  @(rf/subscribe [:counter-value]))