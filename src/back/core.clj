(ns core
  "Main namespace. Include the application entry point function"
  (:require [cli :refer [parse-cli-options usage show-errors help-option?]]
            [server :refer [start]]
            [clojure.java.browse :refer [browse-url]])
  (:gen-class))

(defn open-browser 
  "Try to open URL is default browser"
  [url]
  (try
    (println "opening browser ...")
    (browse-url url)
    (catch Exception e (str "failed to open browser " (.getMessage e)))))


(defn -main [& args]
  (let [parsed-opts (parse-cli-options args)
        errors      (:errors parsed-opts)]
    (cond
      (help-option? parsed-opts)  (println (usage parsed-opts))
      errors                      (println (show-errors errors))
      :else                       (let [port            (get-in parsed-opts [:options :port])
                                        no-open-browser (get-in parsed-opts [:options :no-browser])
                                        url             (format "http://localhost:%d" port)]
                                    (println (format "\n\nstarting server at : %s \n\n " url))

                                    (when-not no-open-browser
                                      (open-browser url))

                                    (start port) ;; blocking call, so browser must be opened first
                                    ))

    (flush)))



