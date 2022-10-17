(ns todo.db-test
  (:require [todo.db :as db]
            [todo.spec :as spectodo]
            [clojure.spec.alpha :as s]

            [clojure.test :refer [deftest is testing]]))

(deftest creation-test
  (testing "create-todo-item when success"
    (is (= #:todo{:text "description", :done false}
           (db/create-todo-item "description" false)))))


(deftest create-test
  (testing "creating an empty db"
    (let [new-db (db/create)]
      (is (coll? (:todo/ordered-ids new-db)))
      (is (map?  (:todo/list new-db)))
      (is (= {:todo/list {}
              :todo/ordered-ids '()}
             new-db)))))


(deftest add-item-to-list-test

  (testing "adding todo item to empty todo list"
    (is (= {"id1" #:todo{:text "description", :done false}}
           (db/add-item-to-list {} "id1" (db/create-todo-item "description" false)))))

  (testing "adding todo item to non empty todo list"
    (is (= {"id1" #:todo{:text "description",
                         :done false},
            "id2" #:todo{:text "another text",
                         :done true}}
           (db/add-item-to-list {"id1" #:todo{:text "description",
                                              :done false}}
                                "id2"
                                (db/create-todo-item "another text" true)))))

  (testing "adding todo item for existing id in the todo list"
    (is (= {"id1" #:todo{:text "another text"
                         :done true}}

           (db/add-item-to-list {"id1" #:todo{:text "description"
                                              :done false}}
                                "id1"
                                (db/create-todo-item "another text" true))))))



(deftest delete-item-from-list-test
  (testing "removing item from todo list"
    (is (= {"id2" #:todo{:text "another text", :done true}}
           (db/delete-item-from-list {"id1" #:todo{:text "description", :done false},
                                      "id2" #:todo{:text "another text", :done true}}
                                     "id1"))))
  (testing "removing last item returns an empty todo list"
    (is (empty? (db/delete-item-from-list {"id2" #:todo{:text "another text", :done true}}
                                          "id2"))))

  (testing "removing item not in list returns same list"
    (is (= {"id2" #:todo{:text "another text", :done true}}
           (db/delete-item-from-list {"id2" #:todo{:text "another text", :done true}} "id1"))))

  (testing "removing item from an empty list returns empty list"
    (is (empty? (db/delete-item-from-list {} "id")))))




(deftest add-item-to-db-test
  (testing "add a todo item to an empty db successfully"
    (is (= {:todo/list   {"id1" #:todo{:text "description"
                                       :done false}}
            :todo/ordered-ids '("id1")}
           (db/add-item {:todo/list   {}
                         :todo/ordered-ids '()}
                        "id1"
                        (db/create-todo-item "description" false)))))

  (testing "add a todo item to an non-empty db successfully"
    (is (= {:todo/list
            {"id1" #:todo{:text "description"
                          :done false}
             "id2" #:todo{:text "description"
                          :done false}}
            :todo/ordered-ids '("id2" "id1")}

           (db/add-item {:todo/list   {"id1" #:todo{:text "description"
                                                    :done false}}
                         :todo/ordered-ids '("id1")}
                        "id2"
                        (db/create-todo-item "description" false)))))

  (testing "add duplicate todo item does not change db"
    (is (= {:todo/list    {"id1" #:todo{:text "description"
                                        :done false}
                           "id2" #:todo{:text "description"
                                        :done false}}
            :todo/ordered-ids '("id2" "id1")}

           (db/add-item {:todo/list    {"id1" #:todo{:text "description"
                                                     :done false}
                                        "id2" #:todo{:text "description"
                                                     :done false}}
                         :todo/ordered-ids '("id2" "id1")}
                        "id2"
                        (db/create-todo-item "description" false))))))





(deftest delete-item-test
  (testing "delete item from db"
    (let [db  {:todo/list    {"id1" #:todo{:text "description"
                                           :done false}
                              "id2" #:todo{:text "description"
                                           :done false}}
               :todo/ordered-ids '("id2" "id1")}
          updated-db (db/delete-item db "id1")]
      (is (nil? (get-in updated-db [:todo/list "id1"])) "id1 is not in the todo-list map anymore")
      (is (= #:todo{:text "description"
                    :done false}
             (get-in updated-db [:todo/list "id2"]))    "remaining item is unchanged")

      (is (empty? (filter #{"id1"} (:todo/ordered-ids updated-db))) "id1 is not in the ordered id list anymore")
      (is (= ["id2"] (:todo/ordered-ids updated-db))                "remaining ids are unchanged in the ordered id list")))

  (testing "deleting item not present in db"
    (let [db  {:todo/list    {"id1" #:todo{:text "description"
                                           :done false}
                              "id2" #:todo{:text "description"
                                           :done false}}
               :todo/ordered-ids '("id2" "id3")}
          updated-db (db/delete-item db "id5")]
      (is (= db updated-db) "db is unchanged"))))




(deftest commit-edit-todo-item-test
  (testing "commit editing an existing item"
    (is (= {"id1" {:text "updated 1"
                   :done true}
            "id2" {:text "text2"
                   :done true}}
           (db/commit-edit-todo-item {"id1" {:text "text1"
                                             :done false}
                                      "id2" {:text "text2"
                                             :done true}}
                                     "id1"
                                     {:text "updated 1"
                                      :done true}
                                     "new-id"))))
  (testing "commit a new item"
    (is (= {"id1" {:text "text1"
                   :done false}
            "new-id" {:text "updated 2"
                      :done true}}
           (db/commit-edit-todo-item {"id1"                {:text "text1"
                                                            :done false}
                                      db/temporary-item-id {:text "text2"
                                                            :done true}}
                                     db/temporary-item-id
                                     {:text "updated 2"
                                      :done true}
                                     "new-id")))))




(deftest commit-ordered-ids-test
  (testing "commit edit existing item"
    (is (= '("id1" "id2" "new-id" "id3")
           (db/commit-ordered-ids (list "id1" "id2" db/temporary-item-id "id3") db/temporary-item-id "new-id"))))

  (testing "commit a new item"
    (is (= '("id1" "id2" "id3")
           (db/commit-ordered-ids (list "id1" "id2" "id3") "id2" "new-id")))))




(deftest commit-todo-item-test
  (testing "commit a todo item"
    (is (= #:todo{:list         {"id1"     #:todo{:text "description", :done false},
                                 "tmp-id"  #:todo{:text "description", :done false},
                                 "new-id2" #:todo{:text "update 2", :done false}},
                  :ordered-ids '("id1" "tmp-id")}
           (db/commit-todo-item {:todo/list    {"id1" #:todo{:text "description"
                                                             :done false}
                                                db/temporary-item-id  #:todo{:text "description"
                                                                             :done false}}
                                 :todo/ordered-ids (list "id1"  db/temporary-item-id)}
                                "new-id2"
                                (db/create-todo-item "update 2" false)))))

  (testing "commit a new todo item"
    (let [result (db/commit-todo-item {:todo/list    {"id1"                 #:todo{:text "description1", :done false} 
                                                       db/temporary-item-id #:todo{:text "description2", :done false}
                                                      "new-id2"             #:todo{:text "description3", :done false}}
                                       :todo/ordered-ids  (list "id1"  db/temporary-item-id "new-id2")}
                                      db/temporary-item-id
                                      (db/create-todo-item "update 2" false))]
      (is (map? result))
      )
    ))