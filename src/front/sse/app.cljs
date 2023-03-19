(ns sse.app
  (:require [reagent.dom :as rdom]
            [reagent.core :as rcore]
            [re-frame.core :as re-frame]
            [goog.string :as gstr]
            [day8.re-frame.http-fx]
            [sse.events :refer [>initialize-state >start-counting >stop-counting]]
            [sse.subs :refer [<counter-value]]))


(defn counter-1 []
  (let [counter-value (<counter-value)]
    [:div
     [:p "In this first démo, click on the \"Start counter\" button to open an SSE channel 
            with the server. Values received are displayed"]
     (when counter-value
       [:p (str "counter : " (first counter-value))])
     [:button {:class "button"
               :on-click #(>start-counting)} "Start counter"]
     [:button {:class "button"
               :on-click #(>stop-counting)} "Stop counter"]]))

(defn app-page []
  [:div
   [:section {:class "section"}
    [:div {:class "container"}
     [:h1 {:class "title"} "SSE Notification"]
     [:p {:class "subtitle"} "Keep up-to-date with server buddy"]
     [:hr]
     [counter-1]]]])

(defn init [element-id]
   ;; Reset routes on figwheel reload
  (>initialize-state)
  (rdom/render [app-page] (js/document.getElementById element-id)))