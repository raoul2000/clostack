(ns default.todo.events
  (:require [re-frame.core :as rf]
            [clojure.string :refer [trim]]
            [ajax.core :as ajax]
            [ajax.edn :refer [edn-response-format edn-request-format]]))

;; when adding an item, a new entry is conjed to the todo list map
;; with a specific and temporary id
(def temp-todo-item-id "-1")

;; ---------------

(defn select-tab-handler
  [db [_ tab-id]]
  (assoc-in db [:todo-widget :selected-tab] tab-id))

(rf/reg-event-db
 :select-tab
 select-tab-handler)

(defn >select-tab
  "User selects a tab"
  [tab-id]
  (rf/dispatch [:select-tab tab-id]))

;; ---------------

(defn quick-filter-update-handler
  [db [_ quick-filter-text]]
  (assoc-in db [:todo-widget :quick-filter] quick-filter-text))

(rf/reg-event-db
 :quick-filter-update
 quick-filter-update-handler)

(defn >quick-filter-update
  "User updates quick filter text value"
  [text]
  (rf/dispatch [:quick-filter-update text]))

;; ---------------

(defn add-todo-item-handler [cofx _]
  ;; by convention, a new todo item has id = "new" until user saves it
  (let [new-item-id temp-todo-item-id
        new-item  {:text ""
                   :done false}]
    {:db (-> (:db cofx)
             (update-in [:todo-widget :todo-list] merge {new-item-id new-item})
             (assoc-in  [:todo-widget :editing-item-id] new-item-id)
             (assoc-in  [:todo-widget :quick-filter] "")
             (assoc-in  [:todo-widget :selected-tab] :tab-all))
     :fx (conj (:fx cofx) [:focus-element-by-id (str "input-" temp-todo-item-id)])}))

(rf/reg-event-fx
 :add-todo-item
 add-todo-item-handler)

(defn >add-todo-item
  "User is starting edition of a new item"
  []
  (rf/dispatch [:add-todo-item]))

;; ---------------

(defn edit-todo-item-handler [cofx [_ todo-item-id]]
  {:db (assoc-in (:db cofx) [:todo-widget :editing-item-id] todo-item-id)
   :fx (conj  (:fx cofx) [:focus-element-by-id (str "input-" todo-item-id)])})

(rf/reg-event-fx
 :edit-todo-item
 edit-todo-item-handler)

(defn >edit-todo-item
  "User is editing an existing todo item with the given id"
  [todo-item-id]
  (rf/dispatch [:edit-todo-item todo-item-id]))

;; -----------------

(defn delete-todo-item [db [_ todo-item-id]]
  (update-in db [:todo-widget :todo-list] dissoc todo-item-id))

(rf/reg-event-db
 :delete-todo-item
 delete-todo-item)

(defn >delete-todo-item
  "User deletes a todo item given its id"
  [todo-item-id]
  (rf/dispatch [:delete-todo-item todo-item-id]))

;; -----------------

(defn cancel-edit-todo-item-handler [db _]
  (let [edited-todo-item-id (get-in db [:todo-widget :editing-item-id])
        updated-db          (assoc-in db  [:todo-widget :editing-item-id] nil)]
    (cond-> updated-db
      ;; canceling edition of a new todo item is like deleting from the todo list
      (= temp-todo-item-id edited-todo-item-id) (update-in [:todo-widget :todo-list] dissoc edited-todo-item-id))))

(rf/reg-event-db
 :cancel-edit-todo-item
 cancel-edit-todo-item-handler)

(defn >cancel-edit-todo-item
  "User cancel todo item edition"
  []
  (rf/dispatch [:cancel-edit-todo-item]))

;; -----------------

(defn next-id [todo-list]
  (str (let [numeric-ids (map #(js/parseInt %) (keys todo-list))]
         (if (empty? numeric-ids)
           1
           (inc (apply max numeric-ids))))))

(defn  commit-edit-todo-item [todo-list todo-item-id todo-item-text]
  (if (= temp-todo-item-id todo-item-id)
    ;; a new item is added: the temp item is committed
    (-> todo-list
        (assoc (next-id todo-list) (-> (get todo-list todo-item-id)
                                       (assoc :text todo-item-text)))
        (dissoc temp-todo-item-id))
    ;; an existing item was updated
    (update todo-list todo-item-id assoc :text todo-item-text)))

(defn save-edit-todo-item-handler [cofx [_ todo-item-text]]
  (let [db                  (:db cofx)
        edited-todo-item-id (get-in   db [:todo-widget :editing-item-id])
        updated-db          (assoc-in db [:todo-widget :editing-item-id] nil)]
    {:db (update-in updated-db [:todo-widget :todo-list] commit-edit-todo-item edited-todo-item-id todo-item-text)
     :fx [[:dispatch [:save-remote]]]}))

(rf/reg-event-fx
 :save-edit-todo-item
 save-edit-todo-item-handler)

(defn >save-edit-todo-item
  "User save todo item after edition. This item can be an existing one being updated
   or a new one being added to the todo list.
   When the given todo item text is empty nothing is done."
  [todo-item-text]
  (let [normalized-text (trim todo-item-text)]
    (when-not (empty?   normalized-text)
      (rf/dispatch [:save-edit-todo-item normalized-text]))))

;; --------------------

(defn toggle-done-handler [cofx [_ todo-item-id]]
  {:db (update-in (:db cofx) [:todo-widget :todo-list todo-item-id] (fn [old-todo-item]
                                                                      (update old-todo-item :done not)))
   :fx (conj (:fx cofx) [:dispatch [:save-remote]])})

(rf/reg-event-fx
 :toggle-done
 toggle-done-handler)

(defn >toggle-done
  "User toggle done status of a todo item given its id"
  [todo-item-id]
  (rf/dispatch [:toggle-done todo-item-id]))

(rf/reg-fx
 :focus-element-by-id
 (fn [element-id]
   (js/setTimeout ;; 'ensure' element is mounted in the DOM 
    #(.focus (.getElementById js/document element-id))
    100)))

;; --------------------

(rf/reg-event-db
 :load-success
 (fn [db [_ response]]
   (update db :todo-widget merge {:todo-list          response
                                  :load-progress      false
                                  :load-error         false
                                  :load-error-message nil})))

(rf/reg-event-db
 :load-error
 (fn [db [_ response]]
   (update db :todo-widget merge {:load-progress      false
                                  :load-error         true
                                  :load-error-message (:last-error response)})))

(defn load-remote-handler [cofx _]
  {:db         (update (:db cofx) :todo-widget merge {:load-progress      true
                                                      :load-error         false
                                                      :load-error-message nil})
   :fx         (or (:fx cofx) [])
   :http-xhrio {:method          :get
                :uri             "/todo"
                :timeout         8000                   ;; optional see API docs
                :response-format (edn-response-format)  ;; IMPORTANT!: You must provide this.
                :on-success      [:load-success]
                :on-failure      [:load-error]}})

(rf/reg-event-fx
 :load-remote
 load-remote-handler)

(defn >load-remote
  "Load Todo list from server"
  []
  (rf/dispatch [:load-remote]))

;; --------------------

(rf/reg-event-db
 :save-success
 (fn [db [_ response]]
   (update db :todo-widget merge {:todo-list          response
                                  :save-progress      false
                                  :save-error         false
                                  :save-error-message nil})))

(rf/reg-event-db
 :save-error
 (fn [db [_ response]]
   (update db :todo-widget merge {:save-progress      false
                                  :save-error         true
                                  :save-error-message (:last-error response)})))

(defn save-remote-handler [cofx _]
  {:db         (update (:db cofx) :todo-widget merge {:save-progress      true
                                                      :save-error         false
                                                      :save-error-message nil})
   :fx         (or (:fx cofx) [])  ;; why ?  because shows warning in console when null
   :http-xhrio {:method          :post
                :uri             "/todo"
                :timeout         8000                   ;; optional see API docs
                :format          (edn-request-format)
                :params          (get-in cofx [:db :todo-widget :todo-list])
                :response-format (edn-response-format)  ;; IMPORTANT!: You must provide this.
                :on-success      [:save-success]
                :on-failure      [:save-error]}})

(rf/reg-event-fx
 :save-remote
 save-remote-handler)

(defn >save-remote
  "Save Todo list to server"
  []
  (rf/dispatch [:save-remote]))

