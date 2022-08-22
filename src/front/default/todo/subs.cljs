(ns default.todo.subs
  (:require [re-frame.core :as rf]
            [clojure.string :refer [includes?]]))

(defn create-initial-state []
  {:todo-widget {:todo-list      {1   {:text  "do something"
                                       :done  false}
                                  2   {:text  "do something else"
                                       :done  true}}
                 :editing-item-id  nil
                 :ordered-ids      [] ;; not used yet
                 :quick-filter     ""
                 :selected-tab     :tab-all}})

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

;; layer 3 -------------------------------------------------


(rf/reg-sub :filtered-todo-list
            :<- [:todo-list]
            :<- [:quick-filter]
            :<- [:selected-tab]
            (fn [[todo-list filter-text selected-tab] _]
              (cond-> todo-list
                (not= "" filter-text)       (->> ,,,
                                             (filter #(includes? (get (second %) :text) filter-text))
                                             (into {}))
                (= selected-tab :tab-todo) (->> ,,,
                                            (filter #(not (get (second %) :done)))
                                            (into {}))
                (= selected-tab :tab-done) (->> ,,,
                                            (filter #(get (second %) :done))
                                            (into {})))))

(defn <filtered-todo-list []
  @(rf/subscribe [:filtered-todo-list]))