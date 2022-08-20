(ns default.todo.views)

(def todo-list [{:id    1
                 :text  "do something"
                 :done  false
                 :edit  false}
                {:id    2
                 :text  "do something else"
                 :done  true
                 :edit  false}
                {:id    3
                 :text  "editing this item"
                 :done  false
                 :edit  true}])

(defn quick-filter []
  [:div.panel-block
   [:p.control.has-icons-left
    [:input.input {:type        "text"
                   :placeholder "filter"}]
    [:span.icon.is-left
     [:i.mdi.mdi-filter {:aria-hidden "true"}]]]])

(defn selector-tabs []
  [:p.panel-tabs
   [:a.is-active "To do"]
   [:a "Done"]
   [:a "All"]])

(defn show-todo-item [text]
  [:<>
   [:span.is-flex-grow-1 text]
   [:div.is-flex.is-justify-content-flex-end
    [:span.panel-icon.todo-action.todo-action-edit
     [:i.mdi.mdi-pencil {:aria-hidden "true"}]]
    [:span.panel-icon.todo-action.todo-action-delete
     [:i.mdi.mdi-delete-forever {:aria-hidden "true"}]]]])

(defn edit-todo-item [text]
  [:<>
   [:span.is-flex-grow-1
    [:input.input {:type        "text"
                   :placeholder "what do you have to do ?"
                   :value       text}]]
   [:div.is-flex.is-justify-content-flex-end
    [:span.panel-icon.todo-action.todo-action-save
     [:i.mdi.mdi-check-circle-outline {:aria-hidden "true"}]]
    [:span.panel-icon.todo-action.todo-action-cancel
     [:i.mdi.mdi-close-circle-outline {:aria-hidden "true"}]]]])

(defn render-todo-item [{:keys [text done edit id]}]
  [:a.panel-block {:key   id
                   :class (when done "mark-done")}
   [:span.panel-icon
    [:i.mdi.mdi-check {:aria-hidden "true"}]]
   (if-not edit
     [show-todo-item text]
     [edit-todo-item text])])

(defn action-bar []
  [:div.panel-block
   [:button.button.is-link.is-outlined.is-fullwidth.is-shadowless "New..."]])

(defn render []
  [:nav.panel
   [:p.panel-heading "Todo"]
   [quick-filter]
   [selector-tabs]
   [:div.todo-list-container
    (map render-todo-item todo-list)]
   [action-bar]])