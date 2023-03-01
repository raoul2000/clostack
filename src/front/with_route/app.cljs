(ns with-route.app
  (:require [reagent.dom :as rdom]
            [re-frame.core :as re-frame]
            [goog.string :as gstr]
            [day8.re-frame.http-fx]
            [reitit.core :as rec]
            [reitit.frontend :as ref]
            [reitit.frontend.easy :as rfe]))

(re-frame/reg-fx :push-state
                 (fn [route]
                   (apply rfe/push-state route)))

(re-frame/reg-event-fx ::push-state
                       (fn [_ [_ & route]]
                         {:push-state route}))

(re-frame/reg-event-db ::navigated
                       (fn [db [_ new-match]]
                         (assoc db :current-route new-match)))

(defn href
  "Return relative url for given route. Url can be used in HTML links."
  ([k]
   (href k nil nil))
  ([k params]
   (href k params nil))
  ([k params query]
   (rfe/href k params query)))

(def routes [["/page1" ::page-1]
             ["/page2" ::page-2]])

(def router (ref/router routes))

(comment
  (rec/routes router)
  (rec/route-names router)
  (rec/match-by-name router ::page-1)

  ;; register the router
  (rfe/start! router (fn [match history] (constantly true)) {:use-fragment true})
  (rfe/href ::page-1)
  (rfe/href ::page-2)

  ;;
  )

(defn on-navigate [new-match]
  (when new-match
    (re-frame/dispatch [::navigated new-match])))

(defn init-routes! []
  (js/console.log "initializing routes")
  (rfe/start!
   router
   on-navigate
   {:use-fragment true}))

(comment
  (init-routes!)

  ;;
  )

(defn navbar []
  [:div
   (interpose " | " [[:a {:href (href ::page-1)}  "page 1"]
                     [:a {:href (href ::page-2)} "page 2"]])])

(defn app-page []

  [:section {:class "section"}

   [:div {:class "container"}
    [:h1 {:class "title"} "The Routed App"]
    [:p {:class "subtitle"} "Powered by "
     [:strong "Reitit"] " library"]
    (navbar)]])

(defn render [element-id]
  (js/console.log "render")
  (rdom/render [app-page] (js/document.getElementById element-id)))
