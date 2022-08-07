(ns default.app
  (:require [reagent.dom :as rdom]
            [goog.string :as gstr]
            [default.views :as views]
            [default.events :refer [>nav >initialize-state]]
            [default.subs :as subs]
            [day8.re-frame.http-fx]
            [re-frame.core :as rf]))

(defn navbar-menu []
  (let [current-route   @(rf/subscribe [:route])
        route-id       (first current-route)]
    [:div.navbar-menu.is-active
     [:div.navbar-start
      [:a.navbar-item {:on-click  #(>nav [:home-route])
                       :class     (when (= :home-route route-id) "is-active")} "Home"]
      [:a.navbar-item {:on-click  #(>nav [:widget-route])
                       :class     (when (= :widget-route route-id) "is-active")} "Widget"]]]))


(defn navbar []
  [:nav.navbar.is-light {:role       "navigation"
                         :aria-label "main navigation"}
   [:div.navbar-brand
    [:a.navbar-item {:href "https://clojurescript.org/"}
     [:img {:src    "/image/cljs-logo.png"
            :width  "28"
            :height "28"}]]]
   [navbar-menu]])

(defn app-page []
  [:div
   [navbar]
     (let [current-route   @(rf/subscribe [:route])
           route-id       (first current-route)]
       (case route-id
         :home-route   [views/home]
         :widget-route [views/say-hi-widget]
         [views/home]))])

(defn render [element-id]
  (>initialize-state)
  (rdom/render [app-page] (js/document.getElementById element-id)))

