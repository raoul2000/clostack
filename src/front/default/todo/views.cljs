(ns default.todo.views
  (:require [reagent.core :as r]
            [clojure.string :refer [blank?]]
            [default.todo.subs :refer [<selected-tab <quick-filter
                                       <editing-item-id <load-progress
                                       <load-error <load-error-message <save-error
                                       <ordered-filtered-todo-list]]
            [default.todo.events :refer [>select-tab >quick-filter-update >add-todo-item
                                         >edit-todo-item >delete-todo-item >cancel-edit-todo-item
                                         >save-edit-todo-item >toggle-done >re-order-items]]
            [goog.dom.classlist :as classlist]))

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

(defn edit-todo-item [id text]
  (let [input-text        (r/atom text)
        update-input-text #(reset! input-text (-> % .-target .-value))]
    (fn []
      [:<>
       [:span.is-flex-grow-1
        [:input.input {:id            (str "input-" id)
                       :type          "text"
                       :placeholder   "what do you have to do ?"
                       :value         @input-text
                       :on-change     update-input-text
                       :on-key-press  #(when (= 13 (.-charCode %)) (>save-edit-todo-item @input-text))}]]
       [:div.is-flex.is-justify-content-flex-end
        [:span.panel-icon.todo-action.todo-action-save
         [:i.mdi.mdi-check-circle-outline {:aria-hidden "true"
                                           :title       "save"
                                           :on-click    #(>save-edit-todo-item @input-text)}]]
        [:span.panel-icon.todo-action.todo-action-cancel
         [:i.mdi.mdi-close-circle-outline {:aria-hidden "true"
                                           :title       "cancel"
                                           :on-click    >cancel-edit-todo-item}]]]])))

(defn drag-start-handler [event]
  (.setData (.-dataTransfer event) "text/plain" (-> event .-target .-id))
  (classlist/add (.-target event) "drag-start"))

(defn drag-enter-handler [event]
  (let [drop-target-element (.closest (.-target event) "[draggable='true']")]
    (.preventDefault event)
    (.stopPropagation event)
    (classlist/add drop-target-element "drag-enter")))

(defn drag-over-handler [event]
  (.preventDefault event))

(defn drag-leave-handler [event]
  (.stopPropagation event)
  (.preventDefault event)
  (classlist/remove (-> event .-target) "drag-enter"))

(defn drop-handler [event]
  (let [dropped-item-id (.getData (.-dataTransfer event) "text/plain")
        target-item-id  (.-id (.closest (.-target event) "[draggable='true']"))]
    (classlist/remove (.-target event) "drag-enter")
    (>re-order-items dropped-item-id target-item-id)
    (.preventDefault event)))

(defn render-todo-item [[id {:keys [text done]}]]
  [:a.panel-block {:id             id
                   :key            id
                   :class          (when done "mark-done")
                   :draggable      "true"
                   :on-drag-start  drag-start-handler
                   :on-drag-enter  drag-enter-handler
                   :on-drag-over   drag-over-handler
                   :on-drag-leave  drag-leave-handler
                   :on-drop        drop-handler}
   [:span.panel-icon
    {:on-click #(>toggle-done id)}
    [:i.mdi.mdi-check {:aria-hidden "true"}]]
   (if (= id (<editing-item-id))
     [edit-todo-item id text]
     [show-todo-item id text])])

(defn action-bar []
  [:div.panel-block
   [:button.button.is-link.is-outlined.is-fullwidth.is-shadowless
    {:on-click >add-todo-item
     :disabled (<editing-item-id)} "New..."]])

(defn render-load-progress  []
  [:div.box
   "Loading your todo list"
   [:progress.progress.is-small.is-info "loading..."]])

(defn render-load-error []
  [:div.box
   [:div.icon-text
    [:span.icon.has-text-danger
     [:i.mdi.mdi-close-octagon.mdi-18px]]
    [:b "Something went wrong !"]]
   [:p "Sorry but it seems that your todo list could not be loaded."]
   (let [error-message (<load-error-message)]
     (when-not (blank? error-message)
       [:div.content
        [:p "The server returns this message : "]
        [:blockquote error-message]]))])

(defn render-todo-list []
  [:<>
   (when (<save-error)
     [:div.box.is-shadowless
      [:div.icon-text
       [:span.icon.has-text-danger
        [:i.mdi.mdi-close-octagon.mdi-18px]]
       "Oups ... it seems your changes could not be saved."]])
   [selector-tabs]
   [quick-filter]
   [:div.todo-list-container
    (doall
     (map render-todo-item (<ordered-filtered-todo-list)))]
   [action-bar]])

(defn render []
  (fn []
    [:nav.panel
     [:p.panel-heading "Todo List"]
     (cond
       (<load-error)    (render-load-error)
       (<load-progress) (render-load-progress)
       :else            (render-todo-list))]))