(ns sse.subs
  (:require [re-frame.core :as rf]))

(defn create-initial-state []
  (merge {:counting false}))