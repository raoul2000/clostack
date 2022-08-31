(ns todo.db-test
  (:require [todo.db :as db]
            [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is testing]]))

(deftest creation-test
  (testing "create-todo-item when success"
    (is (= #:todo{:text "description", :done false}
           (db/create-todo-item "description" false))))

  (testing "create-todo-list when success"
    (is (= {}
           (db/create-todo-list)))))

(deftest create-test
  (testing "creating an empty db"
    (is (= {:todo-list {}
            :ordered-ids []}
           (db/create)))))

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