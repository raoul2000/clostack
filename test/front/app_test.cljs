(ns app-test
  (:require [core :refer [some-func]]
            [utils :refer [add-one]]
            [cljs.test :refer (deftest is)]))

(deftest a-dummy-success-test
  (is (= 1 1)))

(deftest some-func-test
  (is (= 2 (some-func 1))))

(deftest adding-one
  (is (= 2 (add-one 1))))

;;(deftest failing-test
;;  (is (= 1 12) "1 is not equal to 2"))

