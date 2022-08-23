(ns todo
  (:require [response :as resp]))

(def todo-list (atom {1   {:text  "do something funny"
                           :done  false}
                      2   {:text  "do something clever"
                           :done  true}}))

(defn read 
  "Returns the complete todo list"
  [_]
  (resp/ok  @todo-list))

(defn write 
  "Replace the existing todo list with the one provided in the request body"
  [request]
  (if-let  [new-todo-list  (some #(get request %) [:json-params :edn-params])]
    (resp/ok (reset! todo-list new-todo-list))
    (resp/not-found "the request body doesn't contains the todo list")))