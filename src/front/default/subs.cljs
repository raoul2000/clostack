(ns default.subs
  (:require [re-frame.core :as rf]))

;; layer 2

(rf/reg-sub  :username
             (fn [db _]
               (:username db)))