(ns default.todo.events
  (:require [re-frame.core :as rf]
            [clojure.string :refer [trim]]
            [ajax.edn :refer [edn-response-format edn-request-format]]))

;; when adding an item, a new entry is conjed to the todo list map
;; with a specific and temporary id
(def temp-todo-item-id "-1")

;; a custom effect to give focus to an element given its id
(rf/reg-fx
 :focus-element-by-id
 (fn [element-id]
   (js/setTimeout ;; 'ensure' element is mounted in the DOM 
    #(.focus (.getElementById js/document element-id))
    100)))

(def save-todo-list
  "interceptor running after event handler to save todo-list to server"
  (rf/->interceptor {:id :save-todo-list-interc
                     :after (fn [context]       ;; applied after the db is updated
                              (let [todo-list   (get-in context [:effects :db :todo-widget :todo-list])
                                    ordered-ids (get-in context [:effects :db :todo-widget :ordered-ids])]
                                (update-in context [:effects] #(assoc % :http-xhrio {:method          :post
                                                                                     :uri             "/todo"
                                                                                     :format          (edn-request-format)
                                                                                     :params          {:todo-list   todo-list
                                                                                                       :ordered-ids ordered-ids}
                                                                                     :response-format (edn-response-format)
                                                                                     :on-success      [:save-success]
                                                                                     :on-failure      [:save-error]}))))}))

;; --------------- Events definition -----------------------------------

(rf/reg-event-db
 :save-success
 (fn [db [_ response]]
   (update db :todo-widget merge {:todo-list          (:todo-list   response)
                                  :ordered-ids        (:ordered-ids response)
                                  :save-progress      false
                                  :save-error         false
                                  :save-error-message nil})))

(rf/reg-event-db
 :save-error
 (fn [db [_ response]]
   (update db :todo-widget merge {:save-progress      false
                                  :save-error         true
                                  :save-error-message (:last-error response)})))

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
  ;; by convention, a new todo item is assigned a temporary id (see temp-todo-item-id)
  ;; until user actually saves it
  (let [new-item  {:text ""
                   :done false}]
    {:db (-> (:db cofx)
             (update-in [:todo-widget :todo-list]       merge {temp-todo-item-id new-item})
             (update-in [:todo-widget :ordered-ids]     conj temp-todo-item-id)
             (assoc-in  [:todo-widget :editing-item-id] temp-todo-item-id)
             (assoc-in  [:todo-widget :quick-filter]    "")
             (assoc-in  [:todo-widget :selected-tab]    :tab-all))
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
  (-> db
      (update-in [:todo-widget :todo-list]   dissoc todo-item-id)
      (update-in [:todo-widget :ordered-ids] (fn [old-ordered-ids]
                                               (remove #{todo-item-id} old-ordered-ids)))))

(rf/reg-event-db
 :delete-todo-item
 [save-todo-list]
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
  "User cancels todo item edition"
  []
  (rf/dispatch [:cancel-edit-todo-item]))

;; -----------------

(defn next-id
  "Given a todo list, returns the next available id"
  [todo-list]
  (str (let [numeric-ids (map #(js/parseInt %) (keys todo-list))]
         (if (empty? numeric-ids)
           1
           (inc (apply max numeric-ids))))))

(defn  commit-edit-todo-item [todo-list todo-item-id todo-item-text new-id]
  (if (= temp-todo-item-id todo-item-id)
    ;; a new item is added: the temp item is committed
    (-> todo-list
        (assoc new-id (-> (get todo-list todo-item-id)
                          (assoc :text todo-item-text)))
        (dissoc temp-todo-item-id))
    ;; an existing item was updated
    (update todo-list todo-item-id assoc :text todo-item-text)))

(defn commit-ordered-ids [ordered-ids todo-item-id new-id]
  (if (= temp-todo-item-id todo-item-id)
    ;; a new item is added
    (conj (remove #{temp-todo-item-id} ordered-ids) new-id)
    ;; an existing item was updated: ordered ids not modified
    ordered-ids))

(defn save-edit-todo-item-handler [db [_ todo-item-text]]
  (let [edited-todo-item-id (get-in   db [:todo-widget :editing-item-id])
        updated-db          (assoc-in db [:todo-widget :editing-item-id] nil)
        todo-list           (get-in   db [:todo-widget :todo-list])
        new-id              (next-id todo-list)]
    (-> updated-db
        (update-in [:todo-widget :todo-list]   commit-edit-todo-item edited-todo-item-id todo-item-text new-id)
        (update-in [:todo-widget :ordered-ids] commit-ordered-ids    edited-todo-item-id new-id))))

(rf/reg-event-db
 :save-edit-todo-item
 [save-todo-list]
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

(defn toggle-done-handler [db [_ todo-item-id]]
  (update-in db [:todo-widget :todo-list todo-item-id] (fn [old-todo-item]
                                                         (update old-todo-item :done not))))

(rf/reg-event-db
 :toggle-done
 [save-todo-list]
 toggle-done-handler)

(defn >toggle-done
  "User toggle done status of a todo item given its id"
  [todo-item-id]
  (rf/dispatch [:toggle-done todo-item-id]))


;; --------------------

(rf/reg-event-db
 :load-success
 (fn [db [_ response]]
   (update db :todo-widget merge {:todo-list          (:todo-list   response)
                                  :ordered-ids        (:ordered-ids response)
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

(defn re-order-item-ids [list-ids move-id before-id]
  (js/console.log (str "list ids = " list-ids))
  (js/console.log (str "move id = " move-id))
  (js/console.log (str "before id = " before-id))
  (if (= move-id before-id)
    list-ids
    (reduce (fn [res i]
              (cond
                (= i move-id) res
                (= i before-id) (conj res move-id before-id)
                :else (conj res i))) [] list-ids)))


(defn re-order-items-handler [cofx [_ move-item-id before-item-id]]
  (update cofx :db update-in  [:todo-widget :ordered-ids] re-order-item-ids move-item-id before-item-id))

(rf/reg-event-fx
 :re-order-items
 [save-todo-list]
 re-order-items-handler)

(defn >re-order-items [move-item-id before-item-id]
  (rf/dispatch [:re-order-items move-item-id before-item-id]))