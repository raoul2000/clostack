(ns todo.db
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]))

(defn create-todo-item 
  "Given a *text* description and a *done*  flag, creates
   and returns a *Todo* item"
  [text done]
  {:pre [(s/valid? :todo/text  text)
         (s/valid? :todo/done  done)]
   :post [(s/valid? :todo/item %)]}
  {:todo/text  text
   :todo/done  done})


(defn create
  "Create and returns an empty DB to manage todo list"
  []
  {:post [(s/valid? :todo/db %)]}
  {:todo/list        {}
   :todo/ordered-ids []})

(defn new-id 
  "Creates and returns a new identifier"
  []
  (str (random-uuid)))

(def temporary-item-id "tmp-id")

(defn add-item-to-list
  "Assoc *new-item* to key *new-id* in the given todo list. When *new-id* is already
   present in the todo list, its value is updated with *new-item*.
   
   Returns the updated *todo-list*."
  [todo-list new-id new-item]
  {:pre [(s/valid? :todo/list todo-list)
         (s/valid? :todo/id        new-id)
         (s/valid? :todo/item      new-item)]
   :post [(s/valid? :todo/list %)]}
  (assoc todo-list new-id new-item))


(defn delete-item-from-list 
  "Removes item with id *item-id* from *todo-list*, a list of todo items.
   
   Returns the updated *todo-list*."
  [todo-list item-id]
  {:pre [(s/valid? :todo/list todo-list)
         (s/valid? :todo/id  item-id)]
   :post [(s/valid? :todo/list %)]}
  (dissoc todo-list item-id))


(defn add-item
  "Add *item* with identifier *item-id* to the Todo Db *db* and returns the updated *db*. 
   If an item with the same id already exists in *db*, nothing is done."
  [db item-id item]
  {:pre [(s/valid? :todo/db  db)
         (s/valid? :todo/id  item-id)]
   :post [(s/valid? :todo/db %)]}
  (cond-> db
    (not-any? #{item-id} (:todo/ordered-ids db))
    (->
     (update :todo/list add-item-to-list item-id item)
     (update :todo/ordered-ids conj item-id))))

(defn delete-item
  "Remove item with id *item-id* from the *db* and returns updated *db*." 
  [db item-id]
  (-> db
      (update :todo/list dissoc item-id)
      (update :todo/ordered-ids #(remove #{item-id} %))))

(defn  commit-edit-todo-item [todo-list todo-item-id todo-item new-item-id]
  (if (= temporary-item-id todo-item-id)
    ;; a new item is added: the temp item is committed
    (-> todo-list
        (assoc  new-item-id todo-item)
        (dissoc temporary-item-id))
    ;; an existing item was updated
    (assoc todo-list todo-item-id todo-item)))

(defn commit-ordered-ids [ordered-ids todo-item-id new-id]
  (if (= temporary-item-id todo-item-id)
    ;; a new item is added 
    (map #(if (= temporary-item-id %) new-id %) ordered-ids)
    ;; an existing item was updated: ordered ids not modified
    ordered-ids))

(defn commit-todo-item [db todo-id todo-item]
  {:pre [(s/valid? :todo/db    db)
         (s/valid? :todo/id    todo-id)
         (s/valid? :todo/item  todo-item)]
   :post [(s/valid? :todo/db %)]}
  (let [new-item-id (new-id)]
    (-> db
        (update :todo/list        commit-edit-todo-item todo-id todo-item new-item-id)
        (update :todo/ordered-ids commit-ordered-ids    todo-id new-item-id))))

