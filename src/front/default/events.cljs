(ns default.events
  (:require [re-frame.core :as rf]
            [ajax.core :as ajax]))

(rf/reg-event-db
 :initialize-state
 (fn [_ _]
   {:route            [:home-route]   ;; default route is the home page
    :saying-hi        false
    :show-modal-demo  false
    :show-left-drawer false}))

(defn >initialize-state
  "Synchronously dispatch the event to initialize the app state"
  []
  (rf/dispatch-sync [:initialize-state]))

;; signals that the action to say hi to the server is in progress (true) or not (false)
(defn saying-hi-handler [db [_ in-progress]]
  (assoc db :saying-hi in-progress))

(rf/reg-event-db
 :saying-hi
 saying-hi-handler)

;; success on saying hi to the server
(defn said-hi-success-handler [cofx [_ result]]
  {:db (-> (:db cofx)
           (assoc :said-hi-success result)
           (assoc :greet-from-server (:reply result)))
   :fx (conj (:fx cofx)  [:dispatch-later {:ms 1000
                                           :dispatch [:saying-hi false]}])})

(rf/reg-event-fx
 :said-hi-success
 said-hi-success-handler)

;; failed on saying hi to the server
(defn said-hi-failure-handler [cofx [_ result]]
  {:db (assoc (:db cofx) :said-hi-error result)
   :fx (conj  (:fx cofx) [:dispatch [:saying-hi false]])})

(rf/reg-event-fx
 :said-hi-failure
 said-hi-failure-handler)

;; regsiter an effect that displays greet message to the console
(rf/reg-fx
 :say-hi-to-console
 (fn [username]
   (js/setTimeout  (fn []
                     (js/console.log (str "fx: saying hi to " username)))
                   1000)))

;; say hi to the server 
(defn  say-hi-handler
  "Event handler to say hi to the server"
  [cofx [_ username]]
  {:db (assoc (:db cofx) :username username)
   :http-xhrio       {:method          :get
                      :uri             "/greet"
                      :params          {:name username}
                      :timeout         8000                                           ;; optional see API docs
                      :response-format (ajax/json-response-format {:keywords? true})  ;; IMPORTANT!: You must provide this.
                      :on-success      [:said-hi-success]
                      :on-failure      [:said-hi-failure]}
   :say-hi-to-console username
   :fx (conj (:fx cofx) [:dispatch [:saying-hi true]])})

(rf/reg-event-fx
 :say-hi
 say-hi-handler)

(defn >say-hi-to
  "User with name *username* is saying 'Hi' to the server"
  [username]
  (rf/dispatch [:say-hi username]))

;; route -----------------------------------------------

(defn nav-handler [db [_ route]]
  (assoc db :route route))

(rf/reg-event-db
 :nav
 nav-handler)

(defn >nav [route]
  (rf/dispatch [:nav route]))

;; modal demo ---------------------------------------------

(defn show-modal-demo-handler [db [_ show?]]
  (assoc db :show-modal-demo show?))

(rf/reg-event-db
 :show-modal-demo
 show-modal-demo-handler)

(defn >show-modal-demo []
  (rf/dispatch [:show-modal-demo true]))

(defn >hide-modal-demo []
  (rf/dispatch [:show-modal-demo false]))

;; drawer -------------------------------------

(defn show-left-drawer-handler [db [_ show?]]
  (assoc db :show-left-drawer show?))

(rf/reg-event-db
 :show-left-drawer
 show-left-drawer-handler)

(defn >show-left-drawer [show]
  (rf/dispatch [:show-left-drawer show]))