(ns todo.db
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]))

(defn create-todo-item [text done]
  {:pre [(s/valid? :todo/text text)
         (s/valid? :todo/done  done)]
   :post [(s/valid? :todo/item %)]}
  {:todo/text  text
   :todo/done  done})

(comment
  (create-todo-item "e" true))

(defn create-todo-list [] {})
(defn create-ordered-ids [] (list))

(defn create
  "Create an returns an empty DB to manage todo list"
  []
  {:post [(s/valid? :todo/db %)]}
  {:todo-list (create-todo-list)
   :ordered-ids (create-ordered-ids)})

(defn new-id []
  (str (random-uuid)))

(defn add-item-to-list
  "Assoc *new-item* to key *new-id* in the given todo list. When *new-id* is already
   present in the todo list, its value is updated with *new-item*"
  [todo-list new-id new-item]
  {:pre [(s/valid? :todo/todo-list todo-list)
         (s/valid? :todo/id        new-id)
         (s/valid? :todo/item      new-item)]
   :post [(s/valid? :todo/todo-list %)]}
  (assoc todo-list new-id new-item))

(comment

  (add-item-to-list {} "E" (create-todo-item "e" true)))
(defn delete-item-from-list [todo-list item-id]
  {:pre [(s/valid? :todo/todo-list todo-list)
         (s/valid? :todo/id  item-id)]
   :post [(s/valid? :todo/todo-list %)]}
  (dissoc todo-list item-id))


(defn add-item
  "Add an *item* given its *id* and todo *db*.
   
   If *id* already exists the *db* is returns unchanged
   "
  [db item-id item]
  ;;{:pre [(s/valid? :todo/db db)
  ;;       (s/valid? :todo/id  item-id)]
  ;; :post [(s/valid? :todo/db %)]}
  (cond-> db
    (not-any? #{item-id} (:ordered-ids db))
    (-> 
         (update :todo-list add-item-to-list item-id item)
         (update :ordered-ids conj item-id))))


