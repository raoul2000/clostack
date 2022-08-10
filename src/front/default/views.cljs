(ns default.views
  (:require [reagent.core :as r]
            [clojure.string :as s]
            [default.events :refer [>say-hi-to >show-modal-demo >hide-modal-demo]]
            [default.subs :refer [<greet-from-server <saying-hi <show-modal-demo]]))

(defn modal [{:keys [username email country on-submit on-cancel]}]
  (let [form-data (r/atom {:name    username
                           :email   email
                           :country country})]
    (fn []
      [:div.modal.is-active
       [:div.modal-background {:on-click #(js/console.log "click modal")}]
       [:div.modal-content
        [:article.message
         [:div.message-header "Contact"
          [:button.delete {:aria-label "close"
                           :on-click on-cancel}]]
         [:div.message-body
          [:div.field
           [:label.label "Name"]
           [:div.control
            [:input.input {:type        "text"
                           :placeholder "your name"
                           :value       (:name @form-data)
                           :on-change   (fn [e]
                                          (swap! form-data assoc :name (-> e .-target .-value)))}]
            [:p.help "this is the name you will use to login"]]]
          [:div.field
           [:label.label "Email"]
           [:div.control
            [:input.input {:type "text"
                           :value       (:email @form-data)
                           :placeholder "e.g. bob@gmail.com"
                           :on-change   (fn [e]
                                          (swap! form-data assoc :email (-> e .-target .-value)))}]
            [:p.help "we may send you some commercial email and for sure plenty of spam"]]]
          [:div.field
           [:label.label "Country"]
           [:div.control
            [:div.select
             [:select
              {:value     (:country @form-data)
               :on-change (fn [e]
                            (swap! form-data assoc :country (-> e .-target .-value)))}
              [:option "Spain"]
              [:option "Other"]
              [:option "France"]]]]]
          [:div.field.is-grouped
           [:div.control
            [:button.button.is-link {:on-click #(on-submit @form-data)} "Submit"]]
           [:div.control
            [:button.button.is-link.is-light {:on-click on-cancel} "Cancel"]]]]]]])))


(defn modal-demo-widget []
  (let [message (r/atom nil)]
    (fn []
      [:div.panel.is-link
       (when (<show-modal-demo)
         [modal  {:username   "bob"
                  :email      "bob@email.com"
                  :country    "France"
                  :on-submit  #(do
                                 (js/console.log %)
                                 (reset! message (:name %))
                                 (>hide-modal-demo))
                  :on-cancel  #(>hide-modal-demo)}])
       [:div.panel-heading "Modal Demo"]
       [:div.panel-block
        (if-let  [msg @message]
          [:p msg]
          [:p "nothing to say ... yet"])]
       [:div.panel-block
        [:button.button {:on-click #(>show-modal-demo)} "Open Modal"]]])))

(defn say-hi-widget []
  (let [username        (r/atom "")
        update-username #(reset! username (-> % .-target .-value))]
    (fn []
      (let [username-val    @username
            empty-username? (zero? (count (s/trim username-val)))]
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
                                                :disabled empty-username?} "Say Hi"]]]))))

(defn widget []
  [:div.section
   [:div.container [say-hi-widget] [modal-demo-widget]]])

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



