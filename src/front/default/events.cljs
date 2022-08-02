(ns default.events
  (:require [re-frame.core :as rf]
            [ajax.core :as ajax]))

;; signals that the action to say hi to the server is in progress (true) or not (false)
(rf/reg-event-db
 :saying-hi
 (fn [db [_ in-progress]]
   (assoc db :saying-hi in-progress)))

(rf/reg-event-db
 :said-hi-success
 (fn [db [_ result]]
   (js/console.log result)
   (rf/dispatch [:saying-hi false])
   (assoc db :said-hi-success result)))

(rf/reg-event-db
 :said-hi-failure
 (fn [db [_ result]]
   (js/console.log result)
   (rf/dispatch [:saying-hi false])
   (assoc db :said-hi-failure result)))


(rf/reg-fx
 :say-hi-to
 (fn [username]
   (rf/dispatch [:saying-hi true])
   (js/setTimeout  (fn []
                     (js/console.log (str "fx: saying hi to " username))
                     (rf/dispatch [:saying-hi false]))
                   1000)))

(rf/reg-event-fx
 :say-hi
 (fn [cofx [_ username]]
   {:db (assoc (:db cofx) :username username)
    :http-xhrio {:method          :get
                 :uri             "/greet"
                 :timeout         8000                                           ;; optional see API docs
                 :response-format (ajax/json-response-format {:keywords? true})  ;; IMPORTANT!: You must provide this.
                 :on-success      [:said-hi-success]
                 :on-failure      [:said-hi-failure]}
    :say-hi-to username}))

(defn say-hi-to
  "User with name *username* is saying 'Hi' to the server"
  [username]
  (rf/dispatch [:say-hi username]))

