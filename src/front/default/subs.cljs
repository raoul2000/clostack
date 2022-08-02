(ns default.subs
  (:require [re-frame.core :as rf]))

;; layer 2

(rf/reg-sub  :username
             (fn [db _]
               (:username db)))

(rf/reg-sub  :saying-hi
             (fn [db _]
               (:saying-hi db)))