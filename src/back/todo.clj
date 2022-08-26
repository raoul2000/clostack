(ns todo
  (:require [response :as resp]
            [clojure.edn :as edn]
            [babashka.fs :as fs]))

(def base-path
  "A String representing the absolute path to the working dir"
  (str (fs/path (fs/home) ".todos")))

(def todo-file-path (str (fs/path base-path "todo-list.edn")))

(defn prepare-save-to-disk [todo-file-path]
  ;; ensure parent folder exist
  (let [parent-dir (fs/parent todo-file-path)]
    (when-not (fs/exists? parent-dir)
      (fs/create-dirs parent-dir)))
  ;; ensure todo file exists
  (when-not (fs/exists? todo-file-path)
    (println "initiating todo list")
    (spit todo-file-path {})
    (flush)))

(defn write-to-file [todo-list]
  (try
    (prepare-save-to-disk todo-file-path)
    (spit todo-file-path  todo-list)
    (catch Exception e (str "Failed to prepare working dir: " (.getMessage e)))))

(defn read-from-file []
  (try
    (prepare-save-to-disk todo-file-path)
    (edn/read-string (slurp todo-file-path))
    (catch Exception e (str "Failed to prepare working dir: " (.getMessage e)))))

(defn read-todo-list
  "Returns the complete todo list"
  [_]
  (resp/ok  (read-from-file)))

(defn write-todo-list
  "Replace the existing todo list with the one provided in the request body"
  [request]
  (if-let  [new-todo-list (some #(get request %) [:json-params :edn-params])]
    (do
      (write-to-file new-todo-list)
      (resp/ok new-todo-list))
    (resp/not-found "the request body doesn't contains the todo list")))