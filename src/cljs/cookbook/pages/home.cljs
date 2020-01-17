(ns cookbook.pages.home
  (:require
    [re-frame.core :as rf]
    [reagent.core :as r]))

(defn tag-link [tag count]
  (let [selected? (= tag @(rf/subscribe [:selected-tag]))]
    [:a.panel-block
     {:on-click #(rf/dispatch [:set-selected-tag (if selected? nil tag)])
      :class    (if selected? "has-background-warning")}
     (if selected? [:b tag] tag)
     [:span.tag {:style {:margin-left "5px"}}
      count]]))

(defn tag-list []
  (let [tags @(rf/subscribe [:tags])]
    (into
      [:nav.panel
       [:p.panel-heading.is-marginless "Tags"]]
      (for [[tag count] tags]
        [tag-link tag count]))))

(defn recipe-item [recipe]
  [:div.tile.is-parent.is-4
   [:div.tile.is-child.box
    {:on-click #(rf/dispatch [:common/navigate! :cookbook.routes/view-recipe {:id (:id recipe)}])
     :style    {:cursor :pointer}}
    [:p.title.is-3
     (:title recipe)]
    (when (:description recipe)
      [:p.subtitle.is-6
       (:description recipe)])]])

(defn recipe-row [recipes]
  (into
    [:div.tile.is-ancestor]
    (for [recipe recipes]
      [recipe-item recipe])))

(defn recipe-list []
  (let [recipes (rf/subscribe [:recipes])]
    (into
      [:div]
      (for [recipes (partition-all 3 @recipes)]
        [recipe-row recipes]))))

(defn home-page []
  [:section.section>div.container>div.content
   [:div.columns
    [:div.column.is-three-quarters
     [recipe-list]]
    [:div.column
     [tag-list]]
    ]])

#_[:> dnd/DragDropContext
   {:onDragEnd    #()
    :onDragStart  #()
    :onDragUpdate #()}

   ; Example droppable (wraps one of your lists)
   ; Note use of r/as-element and js->clj on droppableProps
   [:> dnd/Droppable {:droppable-id "droppable"}
    (fn [provided snapshot]
      (r/as-element
        [:div (merge {:ref   (.-innerRef provided)
                      :class (when (.-isDraggingOver snapshot) :drag-over)
                      :style {:border "1px solid black"}}
                     (js->clj (.-droppableProps provided)))
         [:h2 "My List - render some draggables inside"]
         ; Example draggable
         [:> dnd/Draggable {:draggable-id "draggable-1", :index 0}
          (fn [provided snapshot]
            (r/as-element [:div (merge {:ref (.-innerRef provided)}
                                       (js->clj (.-draggableProps provided))
                                       (js->clj (.-dragHandleProps provided)))
                           [:p {:style {:border "1px solid black"}} "Drag 1"]]))]
         [:> dnd/Draggable {:draggable-id "draggable-2", :index 1}
          (fn [provided snapshot]
            (r/as-element [:div (merge {:ref (.-innerRef provided)}
                                       (js->clj (.-draggableProps provided))
                                       (js->clj (.-dragHandleProps provided)))
                           [:p {:style {:border "1px solid black"}} "Drag 2"]]))]
         (.-placeholder provided)]))]]