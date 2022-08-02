(ns default.app
  (:require [reagent.dom :as rdom]
            [goog.string :as gstr]
            [default.views :as views]
            [default.events :as events]
            [default.subs :as subs]
            [day8.re-frame.http-fx]))

(defn app-page []
  [:div#someId.someClass
   [:span.nestedClass
    "hello," (gstr/unescapeEntities "&nbsp;") "World !" ;; use HTML entities
    ]
   [views/say-hi-widget]
   [:div
    [:img.clojureLogo {:src "image/clojure-logo.png"}]]])

(defn render [element-id]
  (rdom/render [app-page] (js/document.getElementById element-id)))

