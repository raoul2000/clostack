(ns default.todo.events
  (:require [re-frame.core :as rf]
            [clojure.string :refer [trim]]))

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
  "User is adding a new item"
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
  "User is editing a todo item given its id"
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
    (-> todo-list
        (assoc (next-id todo-list) (-> (get todo-list todo-item-id)
                                       (assoc :text todo-item-text)))
        (dissoc temp-todo-item-id))
    (update todo-list todo-item-id assoc :text todo-item-text)))

(defn save-edit-todo-item-handler [db [_ todo-item-text]]
  (let [edited-todo-item-id (get-in db   [:todo-widget :editing-item-id])
        updated-db          (assoc-in db [:todo-widget :editing-item-id] nil)]
    (update-in updated-db [:todo-widget :todo-list] commit-edit-todo-item edited-todo-item-id todo-item-text)))

(rf/reg-event-db
 :save-edit-todo-item
 save-edit-todo-item-handler)

(defn >save-edit-todo-item
  "User save todo item after edition"
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
 toggle-done-handler)

(defn >toggle-done [todo-item-id]
  (rf/dispatch [:toggle-done todo-item-id]))

(rf/reg-fx
 :focus-element-by-id
 (fn [element-id]
   (js/console.log (str "focus-element-by-id " element-id))
   (js/setTimeout
    #(.focus (.getElementById js/document element-id))
    100)))