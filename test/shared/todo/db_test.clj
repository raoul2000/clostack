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


(deftest add-item-test

  (testing "adding todo item to empty todo list"
    (is (= {"id1" #:todo{:text "description", :done false}}
           (db/add-item {} "id1" (db/create-todo-item "description" false)))))

  (testing "adding todo item to non empty todo list"
    (let [todo-list {"id1" #:todo{:text "description", :done false}}]
      (is (= {"id1" #:todo{:text "description", :done false},
              "id2" #:todo{:text "another text", :done true}}
             (db/add-item todo-list "id2" (db/create-todo-item "another text" true))))))
  
  (testing "adding todo item for existing id in the todo list"
    (let [todo-list {"id1" #:todo{:text "description", :done false}}]
      (is (= {"id1" #:todo{:text "another text", :done true}}
             (db/add-item todo-list "id1" (db/create-todo-item "another text" true))))))
  )