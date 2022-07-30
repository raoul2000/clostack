(ns core
  (:require [cli :refer [parse-cli-options usage show-errors help-option?]]
            [server :refer [start]])
  (:gen-class))

(defn -main [& args]
  (let [parsed-opts (parse-cli-options args)
        errors      (:errors parsed-opts)]
    (cond
      (help-option? parsed-opts)  (println (usage parsed-opts))
      errors                      (println (show-errors errors))
      :else                       (let [port (get-in parsed-opts [:options :port])]
                                    (println (str "starting server on port " port))
                                    (start port)))
    (flush)))



