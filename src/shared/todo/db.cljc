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

(def temporary-item-id "tmp-id")

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
   
   If *id* already exists the *db* is returns unchanged.
   "
  [db item-id item]
  {:pre [(s/valid? :todo/db  db)
         (s/valid? :todo/id  item-id)]
   :post [(s/valid? :todo/db %)]}
  (cond-> db
    (not-any? #{item-id} (:ordered-ids db))
    (->
     (update :todo-list add-item-to-list item-id item)
     (update :ordered-ids conj item-id))))

(defn delete-item
  [db item-id]
  (-> db
      (update :todo-list dissoc item-id)
      (update :ordered-ids #(remove #{item-id} %))))

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
    (doall (map #(if (= temporary-item-id %) new-id %) ordered-ids))
    ;; an existing item was updated: ordered ids not modified
    ordered-ids))

(defn commit-todo-item [db todo-id todo-item]
  {:pre [(s/valid? :todo/db    db)
         (s/valid? :todo/id    todo-id)
         (s/valid? :todo/item  todo-item)]
   :post [(do
            ;;(prn (:ordered-ids %))
            ;;(s/explain :todo/db %)

            (s/valid? :todo/db %))]}
  (let [new-item-id (new-id)]
    (-> db
        (update :todo-list   commit-edit-todo-item todo-id todo-item new-item-id)
        (update :ordered-ids commit-ordered-ids    todo-id new-item-id))))

(comment

  (s/valid? :todo/item #:todo{:text "descr1"    :done false})
  (s/valid? :todo/todo-list {"e" #:todo{:text "descr1"    :done false}
                             "b" #:todo{:text "descr1"    :done false}})

  (s/explain :todo/db {:todo-list {"id1"            #:todo{:text "descr1"    :done false}
                                   temporary-item-id #:todo{:text "descr tmp" :done false}}
                       :ordered-ids (list "id1" temporary-item-id)})


  (list? (list "id1" temporary-item-id))
  (list? '("id1" temporary-item-id))
  (identity '("id1" 'temporary-item-id))

  (def list1 (list "id1" temporary-item-id))
  (commit-todo-item {:todo-list {"id1"              #:todo{:text "descr1"    :done false}
                                 temporary-item-id  #:todo{:text "descr tmp" :done false}}
                     :ordered-ids list1}
                    temporary-item-id
                    (create-todo-item "description" false))



  (s/valid? :todo/db {:todo-list
                      {"id1" #:todo{:text "descr1", :done false},
                       "4a701cbd-d8f7-4dff-8485-75f068ea1b94" #:todo{:text "updated tmp", :done true}},
                      :ordered-ids '("id1" "4a701cbd-d8f7-4dff-8485-75f068ea1b94")})


  (list? (:ordered-ids (commit-todo-item {:todo-list {"id1"  #:todo{:text "descr1"    :done false}
                                                      "id2"  #:todo{:text "descr tmp" :done false}}
                                          :ordered-ids (list "id2" "id1")}
                                         "id2"
                                         #:todo{:text "updated tmp"
                                                :done true})))



  (list? (:ordered-ids (commit-todo-item {:todo-list    {"id1" #:todo{:text "description"
                                                                      :done false}
                                                         "id2" #:todo{:text "description"
                                                                      :done false}}
                                          :ordered-ids (list "id2" temporary-item-id)}
                                         temporary-item-id
                                         (create-todo-item "description" false))))


  (defn do-list [m]
    (update m :l (partial remove  #(= % temporary-item-id))))
  
  (defn do-list2 [m]
    (let [filter-fn (fn [cur-m] (doall (remove #(= % temporary-item-id) cur-m)))]
      (doall (update m :l filter-fn))))

  (let [res (do-list2 {:l (list "a" temporary-item-id)})
        lst (:l res)]
    (println res)
    (println (:l res))
    (println (list? (:l res)))
    (println (seq? (:l res)))
    (println (type (:l res)))
    (type lst)
    
    )
  
  (list? (:l (do-list {:l (list "a" temporary-item-id)})))


  ;; create the initial list
  (def l1 '(:a :b :c))
  (type l1) ;; clojure.lang.PersistentList
  (list? l1) ;; true

  ;; apply filter to create the derived list
  (def l2 (remove (partial = :a) l1))
  (type l2) ;; clojure.lang.LazySeq
  (list? l2) ;; false

  ;; try to force evaluation
  (def l3  (remove (partial = :a) l1))
  (type (doall l3))
  (list? (doall l3))

  ;;
  )
