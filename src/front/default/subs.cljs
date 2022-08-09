(ns default.subs
  (:require [re-frame.core :as rf]))

;; layer 2 ------------------------------------------------------

(rf/reg-sub  :username
             (fn [db _]
               (:username db)))

(rf/reg-sub  :saying-hi
             (fn [db _]
               (:saying-hi db)))

(defn <saying-hi []
  @(rf/subscribe [:saying-hi]))

(rf/reg-sub :greet-response
            (fn [db _]
              (:greet-from-server db)))

(rf/reg-sub :route
            (fn [db _]
              (:route db)))

(rf/reg-sub :show-modal-demo
            (fn [db _]
              (:show-modal-demo db)))

(defn <show-modal-demo []
  @(rf/subscribe [:show-modal-demo]))

;; layer 3  ------------------------------------------------------
;; see http://day8.github.io/re-frame/subscriptions/#reg-sub

(rf/reg-sub :greet-from-server
            :<- [:greet-response]
            (fn [server-response]
              (when server-response 
                (str "server says : \"" server-response "\""))))

(defn <greet-from-server []
  @(rf/subscribe [:greet-from-server]))