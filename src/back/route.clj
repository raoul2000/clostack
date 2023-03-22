(ns route
  "Default Routes dedicated to illustrate some pedestal's feature"
  (:require [clojure.java.io :as io]
            [io.pedestal.http.route :as prt]
            [io.pedestal.http.ring-middlewares :as ring-mw]
            [io.pedestal.http.body-params :refer [body-params]]
            [response :as resp]
            [todo :as todo]
            [babashka.fs :as fs]
            ;; sse
            [io.pedestal.http.sse :as sse]
            [clojure.core.async :as async]
            [clojure.core.async.impl.protocols :as chan]))

(def common-interceptors [resp/coerce-body resp/content-negotiator (body-params)])

;; / -----------------------------------------------------

(defn home [_]
  (resp/ok (slurp (io/resource "public/index.html")) {"Content-Type" "text/html"}))

;; /echo -------------------------------------------------

(def echo
  {:name ::echo
   :enter #(assoc % :response (resp/ok (:request %))) ;; anonymous function to update the :response
                                                      ;; key in the context map
   })

;; /about -------------------------------------------------

(defn about
  "Request handler returing clojure and Java version "
  [_]
  (resp/ok {:clojure-version (clojure-version)
            :java-version    (System/getProperty "java.version")
            :java-vm-version (System/getProperty "java.vm.version")
            :java-vendor     (System/getProperty "java.vendor")}))

;; GET /greet ---------------------------------------------

(defn greeting-for
  "Returns string to greet a given name"
  [name]
  (cond
    (nil? name)            "hello, stranger"
    (#{"bob" "max"} name)  (str "Hi " name "! I know you")
    :else                  (str "hello, " name)))

(defn greet
  "Greet posted name, supporting JSON and EDN body format"
  [request]
  (let [name (some #(get-in request [% :name]) [:query-params :json-params :edn-params])]
    (if-let [greeting (greeting-for name)]
      (resp/ok {:reply greeting})
      (resp/not-found))))

;; POST /upload --------------------------------------------

(defn store-upload-file
  "Store the file being uploaded given a map describing it. Map keys are:

   - `:filename`
   - `:stream`
   - `:content-type`

   The return value will be used as the value for the parameter in the multipart
   parameter map.

   see [wrap-multipart-params](https://ring-clojure.github.io/ring/ring.middleware.multipart-params.html#var-multipart-params-request)"
  [item]
  (let [file-destination-path  (str "c:\\tmp\\" (:filename item))]
    (io/copy (:stream item) (io/file file-destination-path))
    {:file-destination-path file-destination-path}))

(defn upload-progress [request bytes-read content-length item-count]
  (print (format "item : %d - \ncontent length : %d\nbytes-read : %d" item-count content-length bytes-read)))

(defn upload-file [request]
  (resp/ok (:multipart-params request)))

;; GET /download --------------------------------------------------------

(def download-file
  {:name ::download-file-handler
   :enter (fn [context]
            (assoc context :response
                   (resp/ok (fs/file (fs/path (fs/cwd) "test" "back" "sample" "sample.pdf"))
                            ;; set Content-Disposition header to force download.
                            ;; Replace 'attachment' with 'inline' to ask the browser to show the
                            ;; file content
                            {"Content-Disposition" "inline; filename=\"filename.pdf\""}
                            ;; Note that the Content-Type header is set by the ring-mw/file-info interceptor
                            ;; (see route)
                            ;; Other option is to force the Content-Type header :
                            ;; "Content-Type" "image/jpg"
                            )))})

;; GET /sse-notif -----------------------------------------------------------

(defn sse-counter-stream [event-chan context]
  (dotimes [i 10]
    (when-not (chan/closed? event-chan)
      (async/>!! event-chan {:name "counter" :data i})
      (Thread/sleep 500)))
  (async/close! event-chan))

;; async job  -----------------------------------------------------------

(defonce jobs (atom {}))
(defonce job-notif-channel (atom {}))

(defn close-all-notif-job-channels []
  (doseq [notif-channel (vals @job-notif-channel)]
    (async/close! notif-channel))
  (reset! job-notif-channel {}))

(defn job-runner [job-id]
  (when-let [job-info (get @jobs job-id)]
    (future
      (let [notif-channel-id (:channel-id job-info)
            notif-channel    (get @job-notif-channel notif-channel-id)]
        (printf "start job : %s" job-id)
        (async/>!! notif-channel {:name "notif-job" :data (str "start job " job-id)})
        (Thread/sleep 3000)
        (printf "end job : %s" job-id)
        (async/>!! notif-channel {:name "notif-job" :data (str "end job " job-id)}))
      true)))

(defn sse-notif-job 
  "Creates an SSE channel dedicated to notify the client about submited job progress.
   The first message pushed to this SSE channel is the channel Id. This value must be used
   by the client on job submition request."
  [event-chan context]
  (let [channel-id (str (random-uuid))]
    (swap! job-notif-channel assoc channel-id event-chan)
    (async/>!! event-chan {:name "channel-id" :data channel-id})))

(defn create-job 
  "Client creates a job and provide a *channel-id* to be notified about the job progress. The *channel-id* is
   obtains from `/sse-notif-job` path.
   
   The created job is imediately started.
   "
  [{:keys [job-params path-params]}]
  (let [channel-id (:channel-id path-params)
        channel    (get @job-notif-channel channel-id)]
    (cond
      (nil? channel-id) (resp/error-500 "missing channel Id")
      (nil? channel)    (resp/error-500 "invalid channel Id")
      :else             (let [job-id (str (random-uuid))]
                          (swap! jobs assoc job-id {:params     job-params
                                                    :channel-id (:channel-id path-params)})
                          (job-runner job-id)
                          (resp/ok {:job-id job-id})))))

;; Routes -------------------------------------------------

(def default-routes #{["/"       :get  [home] :route-name :home]
                      ["/echo"   :get  [echo] :route-name :echo]
                      ["/about"  :get  (conj common-interceptors about) :route-name :get-about]
                      ["/greet"  :get  (conj common-interceptors greet) :route-name :get-greet]
                      ["/greet"  :post (conj common-interceptors greet) :route-name :post-greet]
                      ["/upload" :post (conj common-interceptors (ring-mw/multipart-params {:store       store-upload-file
                                                                                            :progress-fn upload-progress})
                                             upload-file)               :route-name :post-upload]
                      ["/download"     :get   [;; file-info interceptor will set the content-type of the response
                                               ;; based on the extension of the file to download.
                                               ;; If not set, content-type defaults to application/octet-stream
                                               (ring-mw/file-info)
                                               download-file]           :route-name :get-download]
                      ;; todo resource
                      ["/todo"   :get  (conj common-interceptors todo/read-todo-list)  :route-name :get-todo]
                      ["/todo"   :post (conj common-interceptors todo/write-todo-list) :route-name :post-todo]

                      ;; SSE notifier
                      ["/sse-counter" :get [(sse/start-event-stream sse-counter-stream)] :route-name :get-sse-counter]

                      ;; SSE async job
                      ["/sse-notif-job"   :get (sse/start-event-stream sse-notif-job) :route-name :get-sse-notif-job]

                      ["/job/:channel-id"   :post
                       (conj common-interceptors create-job) :route-name :post-create-job]

                      ;;
                      })

(def routes
  (prt/expand-routes default-routes))