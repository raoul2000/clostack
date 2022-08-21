(ns default.todo.views
  (:require [default.todo.subs :refer [<selected-tab <quick-filter <todo-list
                                       <editing-item-id]]
            [default.todo.events :refer [>select-tab >quick-filter-update >add-todo-item
                                         >edit-todo-item >delete-todo-item >cancel-edit-todo-item
                                         >save-edit-todo-item]]))

(def todo-list {1 {:text  "do something"
                   :done  false
                   :edit  false}
                2 {:text  "do something else"
                   :done  true
                   :edit  false}
                3 {:text  "editing this item"
                   :done  false
                   :edit  true}})

(defn quick-filter []
  [:div.panel-block
   [:p.control.has-icons-left
    [:input.input {:type        "text"
                   :placeholder "filter"
                   :value        (<quick-filter)
                   :on-change  (fn [e]
                                 (>quick-filter-update (-> e .-target .-value)))}]
    [:span.icon.is-left
     [:i.mdi.mdi-filter {:aria-hidden "true"}]]]])

(defn selector-tabs []
  (let [selected-tab (<selected-tab)]
    [:p.panel-tabs
     [:a {:class    (when (= selected-tab :tab-todo) "is-active")
          :on-click #(>select-tab :tab-todo)}
      "To do"]
     [:a {:class    (when (= selected-tab :tab-done) "is-active")
          :on-click #(>select-tab :tab-done)}
      "Done"]
     [:a {:class    (when (= selected-tab :tab-all) "is-active")
          :on-click #(>select-tab :tab-all)}
      "All"]]))

(defn show-todo-item [id text]
  [:<>
   [:span.is-flex-grow-1 text]
   [:div.is-flex.is-justify-content-flex-end
    [:span.panel-icon.todo-action.todo-action-edit
     [:i.mdi.mdi-pencil {:aria-hidden "true"
                         :on-click #(>edit-todo-item id)}]]
    [:span.panel-icon.todo-action.todo-action-delete
     [:i.mdi.mdi-delete-forever {:aria-hidden "true"
                                 :on-click #(>delete-todo-item id)}]]]])

(defn edit-todo-item [text]
  [:<>
   [:span.is-flex-grow-1
    [:input.input {:type        "text"
                   :placeholder "what do you have to do ?"
                   :value       text}]]
   [:div.is-flex.is-justify-content-flex-end
    [:span.panel-icon.todo-action.todo-action-save
     [:i.mdi.mdi-check-circle-outline {:aria-hidden "true"
                                       :on-click >save-edit-todo-item}]]
    [:span.panel-icon.todo-action.todo-action-cancel
     [:i.mdi.mdi-close-circle-outline {:aria-hidden "true"
                                       :on-click >cancel-edit-todo-item}]]]])


(defn render-todo-item [[id {:keys [text done]}]]
  (js/console.log id)
  [:a.panel-block
   {:class (when done "mark-done")
    :key   id}
   [:span.panel-icon
    [:i.mdi.mdi-check {:aria-hidden "true"}]]
   (if (= id (<editing-item-id))
     [edit-todo-item text]
     [show-todo-item id text])])

(defn action-bar []
  [:div.panel-block
   [:button.button.is-link.is-outlined.is-fullwidth.is-shadowless
    {:on-click >add-todo-item
     :disabled (<editing-item-id)} "New..."]])

(defn render []
  [:nav.panel
   [:p.panel-heading "Todo"]
   [quick-filter]
   [selector-tabs]
   [:div.todo-list-container
    (doall (map render-todo-item (<todo-list)))]
   [action-bar]])