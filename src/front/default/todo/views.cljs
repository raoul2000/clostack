(ns default.todo.views
  (:require [reagent.core :as r]
            [default.todo.subs :refer [<selected-tab <quick-filter <todo-list
                                       <editing-item-id <filtered-todo-list]]
            [default.todo.events :refer [>select-tab >quick-filter-update >add-todo-item
                                         >edit-todo-item >delete-todo-item >cancel-edit-todo-item
                                         >save-edit-todo-item >toggle-done]]))

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
     [:a {:class    (when (= selected-tab :tab-all) "is-active")
          :on-click #(>select-tab :tab-all)}
      "All"]
     [:a {:class    (when (= selected-tab :tab-todo) "is-active")
          :on-click #(>select-tab :tab-todo)}
      "To do"]
     [:a {:class    (when (= selected-tab :tab-done) "is-active")
          :on-click #(>select-tab :tab-done)}
      "Done"]]))

(defn show-todo-item [id text]
  [:<>
   [:span.is-flex-grow-1 {:on-click #(>toggle-done id)}
    text]
   [:div.is-flex.is-justify-content-flex-end
    [:span.panel-icon.todo-action.todo-action-edit
     [:i.mdi.mdi-pencil {:aria-hidden "true"
                         :title       "edit"
                         :on-click    #(>edit-todo-item id)}]]
    [:span.panel-icon.todo-action.todo-action-delete
     [:i.mdi.mdi-delete-forever {:aria-hidden "true"
                                 :title       "delete"
                                 :on-click    #(>delete-todo-item id)}]]]])

(defn edit-todo-item [text]
  (let [input-text        (r/atom text)
        update-input-text #(reset! input-text (-> % .-target .-value))]
    (fn []
      [:<>
       [:span.is-flex-grow-1
        [:input.input {:type         "text"
                       :placeholder  "what do you have to do ?"
                       :value        @input-text
                       :on-change    update-input-text
                       :on-key-press  #(when (= 13 (.-charCode %)) (>save-edit-todo-item @input-text))}]]
       [:div.is-flex.is-justify-content-flex-end
        [:span.panel-icon.todo-action.todo-action-save
         [:i.mdi.mdi-check-circle-outline {:aria-hidden "true"
                                           :title "save"
                                           :on-click #(>save-edit-todo-item @input-text)}]]
        [:span.panel-icon.todo-action.todo-action-cancel
         [:i.mdi.mdi-close-circle-outline {:aria-hidden "true"
                                           :title "cancel"
                                           :on-click >cancel-edit-todo-item}]]]])))


(defn render-todo-item [[id {:keys [text done]}]]
  [:a.panel-block
   {:class (when done "mark-done")
    :key   id}
   [:span.panel-icon
    {:on-click #(>toggle-done id)}
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
   [:p.panel-heading "Todo List"]
   [quick-filter]
   [selector-tabs]
   [:div.todo-list-container
    (doall (map render-todo-item (<filtered-todo-list)))]
   [action-bar]])