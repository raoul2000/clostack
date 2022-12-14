(ns default.views
  (:require [reagent.core :as r]
            [clojure.string :as s]
            [default.events :refer [>say-hi-to >show-modal-demo >hide-modal-demo >show-left-drawer]]
            [default.subs :refer [<greet-from-server <saying-hi <show-modal-demo <show-left-drawer]]
            [goog.string :as gstring]
            [goog.string.format]
            [default.todo.views :as todo]
            [goog.dom.classes :as classes]))

;; widgets -----------------------------------------------------------------------------------------

;; modal ----------------

(defn modal [{:keys [username email country on-submit on-cancel]}]
  (let [form-data (r/atom {:username  username
                           :email     email
                           :country   country})]
    (fn []
      [:div.modal.is-active.animate__animated.animate__faster.animate__fadeIn
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
                           :value       (:username @form-data)
                           :on-change   (fn [e]
                                          (swap! form-data assoc :username (-> e .-target .-value)))}]
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
              [:option "England"]
              [:option "Portugal"]
              [:option "Argentina"]
              [:option "France"]
              [:option "Other"]]]]]
          [:div.field.is-grouped
           [:div.control
            [:button.button.is-link {:on-click #(on-submit @form-data)} "Submit"]]
           [:div.control
            [:button.button.is-link.is-light {:on-click on-cancel} "Cancel"]]]]]]])))

(defn make-message [form-data]
  (gstring/format "hello %s ! I will write you an email at %s when I'll come to visit you in %s"
                  (:username form-data)
                  (:email form-data)
                  (:country form-data)))

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
                                 (reset! message (make-message %))
                                 (>hide-modal-demo))
                  :on-cancel  #(>hide-modal-demo)}])
       [:div.panel-heading "Modal Demo"]
       [:div.panel-block
        [:div.content
         [:p "Open the modal window and fill the form with no fear."]
         (when-let  [msg @message]
           [:p msg])]]
       [:div.panel-block
        [:button.button.is-fullwidth.is-link {:on-click #(>show-modal-demo)} "Open Modal"]]])))

;; Say Hi  ----------------

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
;; notification ----------------

(defn notification [{:keys [on-close message close-delay-ms]}]
  (when close-delay-ms
    (js/setTimeout  on-close close-delay-ms))
  [:div.notification.is-primary.is-light.bottom-right.animate__animated.animate__faster.animate__slideInRight
   {:on-animation-end (js/console.log "anmiate end")}
   [:button.delete {:on-click on-close}]
   message])

(defn notification-widget []
  (let [show-notif        (r/atom false)]
    (fn []
      [:div.panel.is-link
       [:div.panel-heading "Notification"]
       [:div.panel-block
        [:div.content
         [:p "Click on the button below to open a notification message"]
         [:p [:b "WARNING : "] " this is not a complete notification feature, just a simple notification being displayed"]]]
       (when @show-notif
         [notification {:message        "This is a notification to inform you that something was successfully done. Good job ! "
                        :on-close       #(reset! show-notif false)
                        :close-delay-ms 5000}])
       [:div.panel-block
        [:button.button.is-fullwidth.is-link {:on-click #(reset! show-notif (not @show-notif))}
         "Show Notif"]]])))

;; drawer ----------------

(defn drawer-control []
  [:div.panel.is-link
   [:div.panel-heading "Drawer"]
   [:div.panel-block
    [:div.content
     [:p "Click on the button below to open the" [:b " left menu"]]
     [:p "the menu is currently " (if (<show-left-drawer) [:span.tag.is-success "opened"] [:span.tag.is-danger "closed"])]]]
   [:div.panel-block
    [:button.button.is-fullwidth.is-link {:on-click #(>show-left-drawer true)}
     "Open"]]])

;; drag and drop ----------------

(defn drag-start-handler [event]
  (js/console.log "drag start")
  (.setData (.-dataTransfer event) "text/plain" (-> event .-target .-id))
  (classes/add (.-target event) "drag-start"))

(defn drag-enter-handler [event]
  (js/console.log "drag enter")
  (js/console.log (-> event .-target))
  (.preventDefault event)
  (classes/add (.-target event) "drag-enter"))

(defn drag-over-handler [event]
  (.preventDefault event))

(defn drag-leave-handler [event]
  (js/console.log "drag leave")
  (classes/remove (-> event .-target) "drag-enter"))

(defn drop-handler [re-order-fn event]
  (js/console.log "drop")
  (js/console.log (str (.getData (.-dataTransfer event) "text/plain")
                       " dropped to " (-> event .-target .-id)))
  (classes/remove (.-target event) "drag-enter")
  (js/console.log (.getData (.-dataTransfer event) "text/plain"))
  (re-order-fn (keyword (.getData (.-dataTransfer event) "text/plain"))
               (keyword (-> event .-target .-id)))
  (.preventDefault event))

(defn render-item [id text re-order-fn]
  [:a.panel-block  {:id             (name id)
                    :key            (name id)
                    :draggable      "true"
                    :on-drag-start  drag-start-handler
                    :on-drag-enter  drag-enter-handler
                    :on-drag-over   drag-over-handler
                    :on-drag-leave  drag-leave-handler
                    :on-drop        (partial drop-handler re-order-fn)}
   text])

(defn re-order [list-item move-item before-item]
  (if (= move-item before-item)
    list-item
    (->> (reduce (fn [res i]
                   (cond
                     (= i move-item) res
                     (= i before-item) (conj res move-item before-item)
                     :else (conj res i))) [] list-item)
         (apply list))))

(defn drag-and-drop-list []
  (let [items (r/atom {:data {:1 "blue" :2 "red" :3 "green" :4 "black" :5 "white" :6 "purple"}
                       :order [:1 :2 :3 :4 :6 :5]})
        re-order-fn (fn [move-id before-id]
                      (swap! items (fn [old-items]
                                     (update old-items :order re-order move-id before-id))))]
    (fn []
      [:div.panel.is-link
       [:div.panel-heading "Drag and drop"]
       (let [item-list   @items
             data        (get item-list :data)
             ordered-ids (get item-list :order)]
         (doall (map (fn [id]
                       (render-item id (get data id) re-order-fn)) ordered-ids)))])))

;; pages -----------------------------------------------------------------------------------------

(defn widget []
  [:div.section
   [:div.columns
    [:div.column.is-3 [say-hi-widget]]
    [:div.column.is-3 [modal-demo-widget]]
    [:div.column.is-3 [notification-widget]]
    [:div.column.is-3 [drawer-control]]]
   [:div.columns
    [:div.column.is-4 [todo/render]]
    [:div.column.is-4 [drag-and-drop-list]]]])

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



