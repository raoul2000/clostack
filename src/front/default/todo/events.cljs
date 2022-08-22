(ns default.todo.events
  (:require [re-frame.core :as rf]))

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

(defn add-todo-item-handler [db _]
  ;; by convention, a new todo item has id = "new" until user saves it
  (let [new-item-id "new"
        new-item  {:text ""
                   :done false}]
    (-> db
        (update-in [:todo-widget :todo-list] merge {new-item-id new-item})
        (assoc-in  [:todo-widget :editing-item-id] new-item-id)
        (assoc-in  [:todo-widget :editing-item]    new-item))))

(rf/reg-event-db
 :add-todo-item
 add-todo-item-handler)

(defn >add-todo-item
  "User is adding a new item"
  []
  (rf/dispatch [:add-todo-item]))

;; ---------------

(defn edit-todo-item-handler [db [_ todo-item-id]]
  (let [editing-item (get-in db [:todo-widget :todo-list todo-item-id])]
    (-> db
        (assoc-in [:todo-widget :editing-item-id] todo-item-id)
        (assoc-in [:todo-widget :editing-item]    editing-item))))

(rf/reg-event-db
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
        updated-db          (-> db
                                (assoc-in  [:todo-widget :editing-item-id] nil)
                                (assoc-in  [:todo-widget :editing-item]    nil))]
    (cond-> updated-db
      ;; canceling edition of a new todo item is like deleting from the todo list
      (= "new" edited-todo-item-id) (update-in [:todo-widget :todo-list] dissoc edited-todo-item-id))))

(rf/reg-event-db
 :cancel-edit-todo-item
 cancel-edit-todo-item-handler)

(defn >cancel-edit-todo-item
  "User cancel todo item edition"
  []
  (rf/dispatch [:cancel-edit-todo-item]))

;; -----------------

(defn save-edit-todo-item-handler [db _]
  (let [edited-todo-item-id (get-in db [:todo-widget :editing-item-id])
        edited-todo-item    (get-in db [:todo-widget :editing-item])
        updated-db          (-> db
                                (assoc-in  [:todo-widget :editing-item-id] nil)
                                (assoc-in  [:todo-widget :editing-item]    nil))]
    (update-in updated-db [:todo-widget :todo-list] (fn [old-todo-list]
                                                      (if (= "new" edited-todo-item-id)
                                                        (let [new-id (inc (apply max (filter int? (keys old-todo-list))))]
                                                          (-> old-todo-list
                                                              (dissoc "new")
                                                              (assoc new-id edited-todo-item)))

                                                        (assoc old-todo-list edited-todo-item-id edited-todo-item))))))

(rf/reg-event-db
 :save-edit-todo-item
 save-edit-todo-item-handler)

(defn >save-edit-todo-item
  "User save todo item after edition"
  []
  (rf/dispatch [:save-edit-todo-item]))

;; --------------------

(defn toggle-done-handler [db [_ todo-item-id]]
  (update-in db [:todo-widget :todo-list todo-item-id] (fn [old-todo-item]
                                                         (update old-todo-item :done not))))

(rf/reg-event-db
 :toggle-done
 toggle-done-handler)

(defn >toggle-done [todo-item-id]
  (rf/dispatch [:toggle-done todo-item-id]))