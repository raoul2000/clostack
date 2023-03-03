(ns with-route.app
  (:require [reagent.dom :as rdom]
            [reagent.core :as rcore]
            [re-frame.core :as re-frame]
            [goog.string :as gstr]
            [day8.re-frame.http-fx]
            [reitit.core :as rec]
            [reitit.frontend :as ref]
            [reitit.frontend.easy :as rfe]))

(re-frame/reg-fx :push-state
                 (fn [route]
                   (println route)
                   (apply rfe/push-state route)))

(comment
  (rec/match-by-name router ::page-1)
  (rfe/push-state (rec/match-by-name router ::page-2))


  (rfe/push-state ::page-2)
  (rfe/push-state ::username-page [:username "bob"])
  (rfe/push-state ::username-page  [:username "bob"])
  (rfe/push-state ::username-page (rec/match-by-path router "/username/bob"))
  ;;
  )

(re-frame/reg-event-fx ::push-state
                       (fn [_ [_ & route]]
                         {:push-state route}))

(re-frame/reg-event-db ::navigated
                       (fn [db [_ new-match]]
                         (assoc db :current-route new-match)))
(re-frame/reg-sub ::current-route
                  (fn [db]
                    (:current-route db)))
(defn href
  "Return relative url for given route. Url can be used in HTML links."
  ([k]
   (href k nil nil))
  ([k params]
   (href k params nil))
  ([k params query]
   (rfe/href k params query)))

(def routes [["/page1" ::page-1]
             ["/page2" ::page-2]
             ["/username/:username" ::username-page]])

(def router (ref/router routes))

(comment
  (rec/routes router)
  (rec/route-names router)
  (rec/match-by-name router ::page-1)
  (rec/match-by-path router "/username/bob")

  ;; register the router
  (rfe/start! router (fn [match history] (constantly true)) {:use-fragment true})
  (rfe/href ::page-1)
  (rfe/href ::page-2)
  (rfe/href ::username-page {:username "bob"})

  (rfe/push-state "#/username/bob")
  (rfe/push-state ::username-page {:username "bill"})
  (href ::username-page {:username "bob"})

  (href ::username-page {:username "bob"})
  (href ::page-1)

  ;;
  )

(defn on-navigate [new-match _]
  (when new-match
    #_(js/console.log new-match)
    (re-frame/dispatch [::navigated new-match])))

(defn init-routes! []
  (js/console.log "initializing routes")
  (rfe/start!
   router
   on-navigate
   {:use-fragment true}))

;; views ------------------------------------------------------------

;; page-1 is just a static div with nothing fancy here
(defn page-1 []
  [:div "Hello from page 1"])

;; page-2 contains a simple form with a local state to store user input (a name)
;; When the form is submitted, the route to show username is called
(defn page-2 []
  (let [username        (rcore/atom "")
        update-username  #(reset! username (-> % .-target .-value))
        submit-username  #(re-frame/dispatch [::push-state ::username-page  {:username @username}])]
    (fn []
      [:div
       [:div {:class "field"}
        [:label {:class "label"} "Name"]
        [:div {:class "control"}
         [:input {:class "input", :type "text", :placeholder "enter username"
                  :value @username
                  :on-change update-username}]]]

       [:div {:class "field is-grouped"}
        [:div {:class "control"}
         [:button {:class "button is-link"
                   :on-click submit-username}
          "Submit"]]]])))

(defn page-username []
  [:div "username"])

(defn navbar []
  (let [current-route @(re-frame/subscribe [::current-route])
        _ (println current-route)]
    [:div
     (interpose " | " [[:a {:key 1
                            :href (href ::page-1)}  "page 1"]
                       [:a {:key 2
                            :href (href ::page-2)} "page 2"]])
     [:hr]
     [:p "some text"]
     (case (get-in current-route [:data :name])
       :with-route.app/page-2 [page-2]
       :with-route.app/page-1 (page-1)
       :with-route.app/page-username (page-username)
       "")]))


(defn app-page []
  [:section {:class "section"}

   [:div {:class "container"}
    [:h1 {:class "title"} "The Routed App"]
    [:p {:class "subtitle"} "Powered by "
     [:strong "Reitit"] " library"]
    (navbar)]])

(defn init [element-id]
  (init-routes!) ;; Reset routes on figwheel reload
  (rdom/render [app-page] (js/document.getElementById element-id)))