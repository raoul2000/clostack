(ns todo-test
  (:require  [clojure.test :refer :all]
             [todo :as td]
             [todo.db :as db]))

(def db1 (-> (db/create)
             (db/commit-todo-item "1" (db/create-todo-item "task 1" true))))


(deftest write-to-file-test
  (testing "save todo to file"
    (let [file-path (td/write-to-file db1)]
      (is (not (empty? file-path))))))

(deftest read-from-file-test
  (testing "read todo from file"
    (is (= #:todo{:list        {"1" #:todo{:text "task 1", :done true}}
                  :ordered-ids []}
           (td/read-from-file)))))

