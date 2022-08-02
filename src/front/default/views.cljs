(ns default.views
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [default.events :refer [say-hi-to]]))

(defn say-hi-widget []
  (let [username (r/atom "")]
    (fn []
      [:div
       [:input {:id        "say-hi"
                :type      :text
                :on-change #(reset! username (-> % .-target .-value))}]
       [:button {:on-click #(say-hi-to  @username)} "Say Hi"]
       [:div @(rf/subscribe [:username])]
       [:div (when @(rf/subscribe [:saying-hi]) "true")]
       
       ])))
