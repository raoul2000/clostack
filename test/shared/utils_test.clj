(ns utils-test
  (:require [utils :refer [add-one]]
            [clojure.test :refer (deftest is)]))

(deftest adding-one
  (is (= 2 (add-one 1))))
