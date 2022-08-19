(ns default.app
  (:require [reagent.dom :as rdom]
            [goog.string :as gstr]
            [default.views :as views]
            [default.events :refer [>nav >initialize-state >show-left-drawer]]
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

(defn left-drawer [& main]
  (let [drawer-is-open  (subs/<show-left-drawer)
        current-route   @(rf/subscribe [:route])
        route-id        (first current-route)]
    [:div
     [:div#mySidenav.sidenav {:style {:width (if drawer-is-open "250px" "0")}}
      [:aside.menu
       [:div.is-clearfix
        [:button.delete.is-pulled-right
         {:on-click #(>show-left-drawer false)}]]
       [:p.menu-label "General"]
       [:ul.menu-list
        [:li [:a {:on-click  #(>nav [:home-route])
                  :class     (when (= :home-route route-id) "is-active")}
              [:span.icon [:i.mdi.mdi-home]] "Home"]]
        [:li [:a {:on-click  #(>nav [:widget-route])
                  :class     (when (= :widget-route route-id) "is-active")}
              [:span.icon [:i.mdi.mdi-widgets-outline]]  "Widget"]]
        [:li [:a {:href "https://cljs.github.io/api/"
                  :target "blank"}
              [:span.icon [:i.mdi.mdi-script-text]] "ClojureScript API"]]
        [:li [:a "Very long Menu item Title that does not fit menu width"]]
        [:li [:a
              [:span.icon [:i.mdi.mdi-alert-circle-outline]] "Customer"]]
        [:li [:a
              [:span.icon [:i.mdi.mdi-cog]] "Preferences"]]]]]
     [:div#main {:style {:marginLeft (if drawer-is-open "250px" "0")}}
      main]]))

(defn app-page []
  [:div
   [left-drawer
    [navbar]
    (let [current-route  @(rf/subscribe [:route])
          route-id       (first current-route)]
      (case route-id
        :home-route   [views/home]
        :widget-route [views/widget]
        [views/home]))]])

(defn render [element-id]
  (js/console.log "render")
  (>initialize-state)
  (rdom/render [app-page] (js/document.getElementById element-id)))

