(ns todo.db
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]))

(defn create-todo-item [text done]
  {:pre [(s/valid? :todo/text text)
         (s/valid? :todo/done  done)]
   :post [(s/valid? :todo/item %)]}
  {:todo/text  text
   :todo/done  done})

(defn create-todo-list [] {})


(defn add-item 
  "Assoc *new-item* to key *new-id* in the given todo list. When *new-id* is already
   present in the todo list, its value is updated with *new-item*" 
  [todo-list new-id new-item]
  {:pre [(s/valid? :todo/todo-list todo-list)
         (s/valid? :todo/id  new-id)
         (s/valid? :todo/item  new-item)]
   :post [(s/valid? :todo/todo-list %)]}
  (assoc todo-list new-id new-item))

