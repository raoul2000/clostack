(ns with-route.app
  (:require [reagent.dom :as rdom]
            [reagent.core :as rcore]
            [re-frame.core :as re-frame]
            [goog.string :as gstr]
            [day8.re-frame.http-fx]
            [reitit.core :as rec]
            [reitit.frontend :as ref]
            [reitit.frontend.easy :as rfe]))

;; create an effect to push a route to the browser history
;; Also trigger a call to the 'on-navigate' callback (see init-routes! below)
(re-frame/reg-fx
 :push-state
 (fn [route]
   (println route)
   (apply rfe/push-state route)))

(comment
  (rec/match-by-name router ::page-1)
  (rfe/push-state (rec/match-by-name router ::page-2))


  (rfe/push-state ::page-2)
  (rfe/push-state ::username-page [:username "bob"])
  (rfe/push-state ::username-page  [:username "bob"])
  (rfe/push-state ::username-page (rec/match-by-path router "/username/bob"))
  ;;
  )

;; register handler for the ::push-state event
;; Set the :push-state effect that will be handled by the registred effect :push-state above.
(re-frame/reg-event-fx
 ::push-state
 (fn [_ [_ & route]]
   {:push-state route}))

;; register a db event handler. The ::navigated event is fired
;; when user request to navigate to another page
(re-frame/reg-event-db
 ::navigated
 (fn [db [_ new-match]]
   (assoc db :current-route new-match)))

;; subscribe to :current-route change and returns it
(re-frame/reg-sub ::current-route
                  (fn [db]
                    (:current-route db)))

;; helper function to create a URL string suitable to be used 
;; as href anchor attribute value
(defn href
  "Return relative url for given route. Url can be used in HTML links.
   Usage :
   ```
   [:a {:href (href ::page-1)}  \"Go to page 1\"]
   [:a {:href (href ::username-page {:username \"bob\"})}  \"Go to Bob page\"]

   ```
   "
  ([k]
   (href k nil nil))
  ([k params]
   (href k params nil))
  ([k params query]
   (rfe/href k params query)))


;; route definition
(def routes [["/page1" ::page-1]
             ["/page2" ::page-2]
             ["/page3" ::page-3]
             ["/username/:username" ::username-page]])

;; create the router
(def router (ref/router routes))

(comment
  (rec/routes router)
  (rec/route-names router)
  (rec/match-by-name router ::page-1)
  (rec/match-by-path router "/username/bob")
  ;;
  )

;; Called each time a new state is pushed (see :push-state effect handler)
(defn on-navigate [new-match _]
  (when new-match
    (re-frame/dispatch [::navigated new-match])))

;; activate Routes 
;; ...registers event listeners on HTML5 history and hashchange events
(defn init-routes! []
  (js/console.log "initializing routes")
  (rfe/start!
   router
   on-navigate
   {:use-fragment true}))

;; views ------------------------------------------------------------

;; page-1 is just a static div with nothing fancy here
(defn page-1 []
  [:div "Hello from page 1"])

;; page-2 contains a simple form with a local state to store user input (a name)
;; When the form is submitted, the route to show username is called
(defn page-2 []
  (let [username        (rcore/atom "")
        update-username #(reset! username (-> % .-target .-value))
        submit-username #(re-frame/dispatch [::push-state ::username-page  {:username @username}])]
    (fn []
      [:div
       [:div {:class "field"}
        [:label {:class "label"} "Name"]
        [:div {:class "control"}
         [:input {:class "input", :type "text", :placeholder "enter username"
                  :value @username
                  :on-change update-username}]]]

       [:div {:class "field is-grouped"}
        [:div {:class "control"}
         [:button {:class "button is-link"
                   :on-click submit-username}
          "Submit"]]]])))

(defn page-3 []
  [:div "Hello from page 3"])

(defn page-username [{:keys [username]}]
  (if (= "bob" username)
    (do
      (js/setTimeout #(re-frame/dispatch [::push-state ::page-2]) 2000)
      [:div "Go away !!"])
    [:div (str "username " username)]))

(defn new-navbar []
  [:nav {:class "navbar is-fixed-top is-transparent", :role "navigation", :aria-label "main navigation"}
   [:div {:class "navbar-brand"}
    [:a {:class "navbar-item", :href "https://bulma.io"}
     [:img {:src "https://bulma.io/images/bulma-logo.png", :width "112", :height "28"}]]]
   [:div {:id "navbarBasicExample", :class "navbar-menu"}
    [:div {:class "navbar-start"}
     [:a {:class "navbar-item"} "hello"]
     [:a {:class "navbar-item"} "item"]]
    [:div {:class "navbar-end"}
     [:div {:class "navbar-item has-dropdown is-hoverable"}
      [:a {:class "navbar-link"} "More ..."]
      [:div {:class "navbar-dropdown is-right"}
       [:a {:class "navbar-item"} "option 1"]
       [:a {:class "navbar-item"} "option 2"]
       [:a {:class "navbar-item"} "option 3"]
       [:hr {:class "navbar-divider"}]
       [:a {:class "navbar-item"} "Report an issue"]]]]]])

(defn navbar []
  (let [current-route @(re-frame/subscribe [::current-route])]
    [:div
     (interpose " | " [[:a {:key 1
                            :href (href ::page-1)}  "page 1"]
                       [:a {:key 2
                            :href (href ::page-2)} "page 2"]
                       [:a {:key 3
                            :href (href ::page-3)} "page 3"]])
     [:hr]
     [:p "I'm displayed on all pages .. isn't that cool ?"]
     (case (get-in current-route [:data :name])
       :with-route.app/page-2 [page-2]
       :with-route.app/page-3 [page-3]
       :with-route.app/page-1 (page-1)
       :with-route.app/username-page (page-username (:path-params current-route))
       "no route match found")]))

(defn app-page []
  [:div
   (new-navbar)
   [:section {:class "section"}

    [:div {:class "container"}
     [:h1 {:class "title"} "The Routed App"]
     [:p {:class "subtitle"} "Powered by "
      [:strong "Reitit"] " library"]
     (navbar)]]])

(defn init [element-id]
  (init-routes!) ;; Reset routes on figwheel reload
  (rdom/render [app-page] (js/document.getElementById element-id)))