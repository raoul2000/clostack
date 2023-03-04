(ns fs
  (:require [babashka.fs :as fs]
            [clojure.string :as s]))


(def re-path-splitter (re-pattern  (str "\\" fs/file-separator)))

(defn split-paths
  "Splits string *path-str* representing a path using local filesystem settings,
   into an array of folder or file names."
  [^String path-str]
  (s/split path-str re-path-splitter))

(defn make-fs-map
  "Given a root *root* and a *glob-pattern* returns a nested map where keys are
   files or folders names and ...
   "
  ([^String root]
   (make-fs-map root "**/*"))
  ([^String root ^String glob-pattern]
   (->> (fs/glob root glob-pattern)
        (map (comp split-paths str))
        (reduce (fn [acc p] (assoc-in acc p {})) {})
       ;;
        )))

(defn ensure-absolute-path [path]
  (if (fs/relative? path) (fs/absolutize path) path))

(defn path-seq 
  "Given *root-path* a folder path, returns a seq of strings representing all
   folder path relative to *root-path*. The path separator is always '/'.
   
   example:
   ```
   (path-seq \"/a/b/c\")
   => (\"d1\"
       \"d1/e\"
       \"d1/e/f\"
       \"d1/e/f\"
       \"d2\")
   ```
   If *root-path* is not an absolute path, it is assumed to be relative to
   the current working dir.
   "
  [root-path]
  (let [absolute-root-path (fs/normalize (ensure-absolute-path root-path))
        relativize-to-root (partial fs/relativize absolute-root-path)]
    (->> (fs/glob absolute-root-path "**")
         (filter fs/directory?)
         (map (comp #(s/join "/" %)
                    split-paths
                    str
                    relativize-to-root)))))

(def obj-type-def {"type1" {:meta? true}})


(defn read-folder [folder-path]
  (fs/list-dir folder-path))

;; Categories are organized as a tree
;; A category :
;;  - MUST have an identifier
;;  - CAN have metadata describing it
;;
;; Categories CAN contain:
;;  - objects
;;  - other categories (sub-categories)
;; => there can be empty categories
;;
;; An object:
;;  - MUST have an identifier
;;  - MUST have a content 
;;  - CAN have metadata : data describing the object

;; Implementation DBFS
;; -------------------
;; Given a folder declared as the "base-path".
;;
;; Categories are sub-folders of the base-path
;;  - category name: path to a folder relative to base-path
;;  - category metadata: file stored in the folder with name '.meta'
;;
;; Objects are files
;;  - object identifier : file title
;;  - object content: object identifier with any extension except 'meta'
;;  - object metadata: object identifier with extension 'meta'
;; 


(comment
  (read-folder "./test/fixture/fs/root/folder-1")
  (map (fn [obj] 
         )(read-folder "./test/fixture/fs/root/folder-1"))
  )