(ns todo.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]))

(def not-blank-trimmed-string? (s/and string? #(not (str/blank? (str/trim %)))))

(s/def :todo/id         not-blank-trimmed-string?)
(s/def :todo/text       string?)
(s/def :todo/done       boolean?)

(s/def :todo/item       (s/keys :req [:todo/text
                                      :todo/done]))

(s/def :todo/list        (s/map-of :todo/id :todo/item))
(s/def :todo/ordered-ids (s/coll-of :todo/id
                                    :distinct true))

(s/def :todo/db (s/keys :req [:todo/list :todo/ordered-ids]))

(comment

  (def my-db {:todo/list {"id1" {:todo/text "buy some milk"
                                 :todo/done false}
                          "id2" {:todo/text "call bob"
                                 :todo/done false}}
              :todo/ordered-ids []})


  (s/valid? :todo/db my-db)
  (s/explain :todo/db my-db))