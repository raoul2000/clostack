(ns experimtent
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]))

(s/def :exp/username   string?)
(s/def :exp/players   (s/coll-of :exp/username
                                 :kind     seq?
                                 :distinct true))

(s/def :exp/capital-players   (s/coll-of :exp/username
                                         :kind     list?))

(defn capitalize [players]
  {:pre  [(s/valid? :exp/players  players)]
   :post [(s/valid? :exp/players  %)]}
  (map str/capitalize (conj players "tom2"))

 ;; (apply list (map str/capitalize players))
  )

(defn f2 [s]
  (println s)
  s)


(s/def :exp/list-of-int (s/coll-of int?))

(comment
  (s/valid? :exp/list-of-int [1 2 3])
  (s/valid? :exp/list-of-int [1 2 "b"])

  
  (def lzsq-ok (map (fn [i]
                   (println i)
                   (inc i)) [1 2 3]))
  (def lzsq-ko (map (fn [i]
                   (println i)
                   (identity i)) [1 2 3 :a]))

  (type lzsq-ok)
  (s/valid? :exp/list-of-int lzsq-ok)
  (s/valid? :exp/list-of-int lzsq-ko)

  
  (type '(1 2 3))
  (type (map str/capitalize ["bob", "alice", "tom"]))
  (type (doall (map str/capitalize ["bob", "alice", "tom"])))


  (type (apply list (doall (map str/capitalize ["bob", "alice", "tom"]))))

  (seq? (map f2 ["bob", "alice", "tom"]))

  (list? (map f2 ["bob", "alice", "tom"]))
  (list? (apply list (map f2 ["bob", "alice", "tom"]))))


(defn start []
  (capitalize  '("bob", "alice", "tom")))