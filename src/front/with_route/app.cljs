(ns with-route.app
  (:require [reagent.dom :as rdom]
            [reagent.core :as rcore]
            [re-frame.core :as re-frame]
            [goog.string :as gstr]
            [day8.re-frame.http-fx]
            [reitit.core :as rec]
            [reitit.frontend :as ref]
            [reitit.frontend.easy :as rfe]))

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

(defn redirect
  "Redirect browser page to *page-id*"
  [page-id]
  (re-frame/dispatch [::push-state page-id]))

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
         [:input {:class "input", :type "text", :placeholder "enter username ... but 'bob' is not welcome"
                  :value @username
                  :on-change update-username}]]]

       [:div {:class "field is-grouped"}
        [:div {:class "control"}
         [:button {:class "button is-link"
                   :on-click submit-username}
          "Submit"]]]])))

(defn page-username
  "Displays the name entered by the user... but 'bob' is not welcome here
   and will be redirected to the username form."
  [{:keys [username]}]
  (if (= "bob" username)
    (do
      (js/setTimeout #(redirect ::page-2) 2000)
      [:div "You've been warned " username ". Go away !!"])
    [:div (str "hello " username ", how are you going today ?")]))

(defn page-3 []
  [:div "Hello from page 3"])

(defn top-navbar []
  (let [{{name :name} :data}  @(re-frame/subscribe [::current-route])]

    [:nav {:class "navbar is-fixed-top is-shadowless", :role "navigation", :aria-label "main navigation"}
     [:div {:class "navbar-brand"}
      [:a {:class "navbar-item", :href "https://bulma.io"}
       [:img {:src "https://bulma.io/images/bulma-logo.png", :width "112", :height "28"}]]]
     [:div {:id "navbarBasicExample", :class "navbar-menu"}
      [:div {:class "navbar-start"}
       [:a {:class (str "navbar-item " (when (= name ::page-1) " is-tab is-active"))
            :href (href ::page-1)}  "page 1"]
       [:a {:class (str "navbar-item " (when (= name ::page-2) " is-tab is-active"))
            :href (href ::page-2)} "page 2"]
       [:a {:class (str "navbar-item " (when (= name ::page-3) " is-tab is-active"))
            :href (href ::page-3)} "page 3"]]
      [:div {:class "navbar-end"}
       [:div {:class "navbar-item has-dropdown is-hoverable"}
        [:a {:class "navbar-link"} "More ..."]
        [:div {:class "navbar-dropdown is-right"}
         [:a {:class "navbar-item"} "option 1"]
         [:a {:class "navbar-item"} "option 2"]
         [:a {:class "navbar-item"} "option 3"]
         [:hr {:class "navbar-divider"}]
         [:a {:class "navbar-item"} "Report an issue"]]]]]]))

(defn main []
  (let [{path-params :path-params
         {view :view, name :name} :data}  @(re-frame/subscribe [::current-route])]
    [:div
     [:hr]
     #_[view path-params]
     [#(when view
         (view path-params)
         #_(redirect ::page-1))]]))

(defn app-page []
  [:div
   (top-navbar)
   [:section {:class "section"}
    [:div {:class "container"}
     [:h1 {:class "title"} "The Routed App"]
     [:p {:class "subtitle"} "Powered by "
      [:strong "Reitit"] " library"]
     (main)]]])

;; Routes -------------------------------------------------------------------------

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

(def routes [["/page1" {:name ::page-1
                        :view page-1}]
             ["/page2" {:name ::page-2
                        :view page-2}]
             ["/page3" {:name ::page-3
                        :view page-3}]
             ["/username/:username" {:name ::username-page
                                     :view page-username}]])

;; create the router
(def router (ref/router routes))

(comment
  (rec/routes router)
  (rec/route-names router)
  (rec/match-by-name router ::page-1)
  (rec/match-by-path router "/username/bob")

  (let [{{:keys [name view]} :data :as all} (rec/match-by-path router "/username/bob")]
    [all name view])


  (let [{path-params :path-params
         {name :name
          view :view} :data} (rec/match-by-path router "/username/bob")]
    [path-params name view])

  (let [{path-params :path-params
         {name :name
          view :view} :data} (rec/match-by-path router "/page1")]
    [path-params name view])

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

;; ---------------------------------------------------------------------------

(defn init [element-id]
  (init-routes!)
  (rdom/render [app-page] (js/document.getElementById element-id)))