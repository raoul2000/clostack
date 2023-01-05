(ns fs
  (:require [babashka.fs :as fs]
            [clojure.string :as s]))


(defn split-paths
  [path-str]
  (s/split path-str (re-pattern  (str "\\" fs/file-separator))))


(defn make-fs-map 
  "Given a root *root* and a *glob-pattern* returns a nested map where keys are
   files or folders names and ...
   "
  [^String root ^String glob-pattern]
  (->> (fs/glob root glob-pattern)
       (map (comp split-paths str))
       (reduce (fn [acc p] (assoc-in acc p {})) {})
       ;;
       )
  )
(comment
  (fs/home)
  ;;System.getenv ("APPDATA")
  (System/getenv "APPDATA")

  (fs/glob "./test/fixture" "**/*.txt")
  (fs/normalize (first (fs/glob "./test/fixture" "**/*")))
  (def path (first (fs/glob "./test/fixture" "**/*")))
  (.split (str path) fs/file-separator)
  (fs/components "aa/zz/ee")
  (fs/split-paths "aa/zz/ee")

  (split-paths "aaa\\zee\\rr")

  (assoc-in {} ["a" "b" "c"] 1)

  (fs/walk-file-tree "./test/fixture" {:pre-visit-dir (fn [dir attr]  (println dir attr) :continue)
                                       :visit-file (fn [f attr] (println f) :continue)})

  (->> (fs/glob "./test/fixture" "**/*.txt")
       ;;(filter fs/directory?)
       (map (comp split-paths str))
       (reduce (fn [acc p] (assoc-in acc p {})) {})
       ;;
       )

  ;;
  )