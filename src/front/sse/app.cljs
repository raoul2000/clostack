(ns sse.app
  (:require [reagent.dom :as rdom]
            [reagent.core :as rcore]
            [re-frame.core :as re-frame]
            [goog.string :as gstr]
            [day8.re-frame.http-fx]
            [sse.events :refer [>initialize-state >start-counting]]))

(defn app-page []
  [:div
   [:section {:class "section"}
    [:div {:class "container"}
     [:h1 {:class "title"} "SSE Notification"]
     [:p {:class "subtitle"} "Keep up-to-date with server buddy"]
     [:hr]
     [:button {:class "button"
               :on-click #(>start-counting)} "count for me"]
     ()]]])

(defn init [element-id]
   ;; Reset routes on figwheel reload
  (>initialize-state)
  (rdom/render [app-page] (js/document.getElementById element-id)))