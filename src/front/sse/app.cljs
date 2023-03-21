(ns sse.app
  (:require [reagent.dom :as rdom]
            [reagent.core :as rcore]
            [re-frame.core :as re-frame]
            [goog.string :as gstr]
            [day8.re-frame.http-fx]
            [sse.events :refer [>initialize-state >start-counting >stop-counting]]
            [sse.subs :refer [<counter-value]]))


(defn counter-value []
  (let [counter-value (<counter-value)]
    [:div {:class "box is-shadowless"}
     [:progress {:class "progress is-info", :value ,(* 10 (first counter-value)) :max "90"} "15%"]]))

(defn endless-counter-card []
  [:div {:class "card"}
   [:header {:class "card-header"}
    [:p {:class "card-header-title is-size-4"} "Endless Counter"]]
   [:div {:class "card-content"}
    [:div {:class "content"}
     "In this first démo, the server exposes an SSE Counter channel that will count to 10 and then close"
     [:br]
     "Click on " [:strong "Start Counter"]
     " to open this channel and see the counter incrementing."
     " You'll notice that when 10 is reached, although the server closes"
     " the SSE channel, the " [:strong "client re-connects"]
     " starting a new round of count. To stop this, click on " [:strong "Stop counter"]
     " and the SSE channel will not be re-opened by the client."
     [counter-value]]]

   [:footer {:class "card-footer"}
    [:a {:href "#", :class "card-footer-item", :on-click #(>start-counting)} "Start Counter"]
    [:a {:href "#", :class "card-footer-item", :on-click #(>stop-counting)}  "Stop Counter"]]])


(defn app-page []
  [:div
   [:section {:class "section"}
    [:div {:class "container"}
     [:h1 {:class "title"} "SSE Notification"]
     [:p {:class "subtitle"} "Keep up-to-date with server buddy"]
     [:hr]
     [:div {:class "columns"}
      [:div {:class "column"}
       [endless-counter-card]]
      [:div {:class "column"} "Second column"]]]]])

(defn init [element-id]
  (>initialize-state)
  (rdom/render [app-page] (js/document.getElementById element-id)))