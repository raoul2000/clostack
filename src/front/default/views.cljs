(ns default.views
  (:require [reagent.core :as r]
            [clojure.string :as s]
            [default.events :refer [>say-hi-to]]
            [default.subs :refer [<greet-from-server <saying-hi]]))

(defn say-hi-widget []
  (let [username        (r/atom "")
        update-username #(reset! username (-> % .-target .-value))]
    (fn []
      (let [username-val    @username
            empty-username? (zero? (count (s/trim username-val)))]
        [:div.section
         [:div.container
          [:div.panel.is-link
           [:div.panel-heading "Please Behave !"]
           [:div.panel-block
            [:div.content
             [:p "Being polite is important and here is the oportunity to salute the server."]
             [:p "The server already knows 'bob' and 'max', but if you're not one fo them, you'll receive greetings too."]]]
           [:div.panel-block
            [:input.input {:id          "say-hi"
                           :placeholder "what's your name ?"
                           :type        :text
                           :on-change   update-username}]]
           [:div.panel-block
            [:p (<greet-from-server)]] ;; server response
           [:div.panel-block
            [:button.button.is-fullwidth.is-link {:class    [(when (<saying-hi) "is-loading")]
                                                  :on-click #(>say-hi-to @username)
                                                  :disabled empty-username?} "Say Hi"]]]]]))))

(defn home []
  [:div
   [:section.hero.is-medium.is-link
    [:div.hero-body
     [:p.title "Clostack"]
     [:p.subtitle "A fullstack Clojure(script) boilerplate app"]]]
   [:div.section
    [:div.columns.is-centered

     [:div.column.is-3
      [:div.content
       [:h1 "Clojure(Script)"]
       [:p "Clojure for the backend, Clojurescript for the front end, "
        [:b "Clostack"]
        " is Complete cross-platform application boilerplate, ready to run on any Java VM."]]]

     [:div.column.is-3
      [:div.content
       [:h1 "Strong Foundation"]
       [:p "Based on "
        [:a {:href "http://day8.github.io/re-frame/"
             :target "pedestal"} "Re-frame"]
        "  and "
        [:a {:href "http://pedestal.io/"
             :target "pedestal"} "Pedestal"]
        ", two powerful frameworks implementing the same "
        [:a {:href "https://en.wikipedia.org/wiki/Interceptor_pattern"
             :target "interceptor"} "Interceptor"]
        " pattern."]]]

     [:div.column.is-3
      [:div.content
       [:h1 "Ready To Build"]
       [:p "A complete set of tooling ready to use, with test and examples. Build the front then build the"
           " complete app with simple commands."]]]]]])


