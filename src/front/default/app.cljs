(ns default.app
  (:require [reagent.dom :as rdom]
            [goog.string :as gstr]))

(defn app-page []
  [:div#someId.someClass
   [:span.nestedClass
    "hello," (gstr/unescapeEntities "&nbsp;") "World !" ;; use HTML entities
    ]
   [:div
    [:img.clojureLogo {:src "image/clojure-logo.png"}]]])

(defn render [element-id]
  (rdom/render [app-page] (js/document.getElementById element-id)))

