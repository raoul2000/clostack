(ns default.views
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [clojure.string :as s]
            [default.events :refer [say-hi-to]]))

(defn say-hi-widget []
  (let [username (r/atom "")]
    (fn []
      (let [username-val @username
            empty-username? (zero? (count (s/trim username-val)))]
        [:div.widget-container
         [:h1 "Please behave !"]
         
         [:input {:id        "say-hi"
                  :type      :text
                  :on-change #(reset! username (-> % .-target .-value))}]
         
           [:button {:on-click #(say-hi-to @username)
                     :disabled empty-username?} "Say Hi"]

         (when-let [username-val @(rf/subscribe [:username])]
           [:div (str "you, " username-val ", want to greet the server")])

         (when @(rf/subscribe [:saying-hi])
           [:div "saying hi ..."])

         [:div  @(rf/subscribe [:greet-from-server])]]))))
