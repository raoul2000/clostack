(ns default.todo.subs
  (:require [re-frame.core :as rf]
            [clojure.string :refer [includes? lower-case trim]]
            [default.todo.events :refer [>load-remote]]))


(def state-sample {:todo-widget {:todo-list         {"1"   {:text  "do something"
                                                            :done  false}
                                                     "2"   {:text  "do something else"
                                                            :done  true}}
                                 :editing-item-id     nil
                                 :ordered-ids         [] ;; not used yet
                                 :quick-filter        ""
                                 :selected-tab        :tab-all
                                 :load-progress       false
                                 :load-error          true
                                 :load-error-message  "invalid address in header"
                                 :save-progress       false
                                 :save-error          true
                                 :save-error-message  "some error occured"}})

(defn create-initial-state []
  (>load-remote)
  {:todo-widget {:todo-list         {}
                 :editing-item-id     nil
                 :ordered-ids         [] ;; not used yet
                 :quick-filter        ""
                 :selected-tab        :tab-all
                 :load-progress       false
                 :load-error          false
                 :load-error-message  ""
                 :save-progress       false
                 :save-error          false
                 :save-error-message  ""}})

;; layer 2 ------------------------------------------------------

(rf/reg-sub  :todo-list
             (fn [db _]
               (get-in db [:todo-widget :todo-list])))

(defn <todo-list []
  @(rf/subscribe [:todo-list]))

(rf/reg-sub  :quick-filter
             (fn [db _]
               (get-in db [:todo-widget :quick-filter])))

(defn <quick-filter []
  @(rf/subscribe [:quick-filter]))

(rf/reg-sub  :selected-tab
             (fn [db _]
               (get-in db [:todo-widget :selected-tab])))

(defn <selected-tab []
  @(rf/subscribe [:selected-tab]))

(rf/reg-sub :editing-item-id
            (fn [db _]
              (get-in db [:todo-widget :editing-item-id])))
(defn <editing-item-id
  "subscribe to the id of the todo item being edited"
  []
  @(rf/subscribe [:editing-item-id]))

(rf/reg-sub :load-progress
            (fn [db _]
              (get-in db [:todo-widget :load-progress])))

(defn <load-progress []
  @(rf/subscribe [:load-progress]))

(rf/reg-sub :load-error
            (fn [db _]
              (get-in db [:todo-widget :load-error])))

(defn <load-error []
  @(rf/subscribe [:load-error]))

(rf/reg-sub :load-error-message
            (fn [db _]
              (get-in db [:todo-widget :load-error-message])))

(defn <load-error-message []
  @(rf/subscribe [:load-error-message]))

;; -------------------------------

(rf/reg-sub :save-error
            (fn [db _]
              (get-in db [:todo-widget :save-error])))

(defn <save-error []
  @(rf/subscribe [:save-error]))

;; -------------------------------

(rf/reg-sub :save-error-message
            (fn [db _]
              (get-in db [:todo-widget :save-error-message])))

(defn <save-error-message []
  @(rf/subscribe [:save-error-message]))

;; layer 3 -------------------------------------------------


(rf/reg-sub :filtered-todo-list
            :<- [:todo-list]
            :<- [:quick-filter]
            :<- [:selected-tab]
            (fn [[todo-list filter-text selected-tab] _]
              (cond-> todo-list
                (not= "" filter-text)       (->> ,,,
                                             (filter #(includes? (lower-case (trim (get (second %) :text))) 
                                                                 (lower-case (trim filter-text))))
                                             (into {}))
                (= selected-tab :tab-todo) (->> ,,,
                                            (filter #(not (get (second %) :done)))
                                            (into {}))
                (= selected-tab :tab-done) (->> ,,,
                                            (filter #(get (second %) :done))
                                            (into {})))))

(defn <filtered-todo-list []
  @(rf/subscribe [:filtered-todo-list]))