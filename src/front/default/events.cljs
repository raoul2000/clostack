(ns default.events
  (:require [re-frame.core :as rf]))

(rf/reg-event-db
 :say-hi
 (fn [db [_ username]]
   (assoc db :username username)))

(defn say-hi-to 
  "User with name *username* is syaing 'Hi' to the server"
  [username]
  (rf/dispatch [:say-hi username]))