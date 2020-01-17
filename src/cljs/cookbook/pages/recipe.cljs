(ns cookbook.pages.recipe
  (:require
    [markdown.core :refer [md->html]]
    [re-frame.core :as rf]
    [cookbook.widgets.core :as widgets]))

(defn raw-html [html]
  {:dangerouslySetInnerHTML {:__html html}})

(defn edit-recipe-page []
  [:section.section>div.container>div.content
   [widgets/text-input "Title" :title]
   [widgets/text-input "Description" :description]
   [widgets/tag-input "Tags" :tags]
   [widgets/item-list "Ingredients" :ingredients]
   [widgets/md-editor "Directions" :directions]
   [:hr]
   [:div.buttons.is-pulled-right
    [:button.button
     {:on-click #(rf/dispatch [:common/navigate! :cookbook.routes/view-recipe (:path @(rf/subscribe [:common/route-params]))])}
     "Cancel"]
    [:button.button.is-warning
     {:on-click #(rf/dispatch [:http/save-recipe])}
     "Save"]]])

(defn view-recipe-page []
  (let [recipe @(rf/subscribe [:recipe])]
    [:section.section>div.container>div.content
     [:div.box.is-clearfix
      [:h2.title.is-2 (:title recipe)]
      [:p.subtitle.is-6 (:description recipe)]
      (into
        [:div.tags]
        (for [tag (:tags recipe)]
          [:span.tag.is-warning tag]))
      [:hr]
      [:h3 "Ingredients"]
      (into
        [:ul]
        (for [ingredient (:ingredients recipe)]
          [:li (raw-html (md->html ingredient))]))
      [:hr]
      [:h3 "Directions"]
      [:div (raw-html (md->html (:directions recipe)))]
      [:hr]
      [:div.is-pulled-left
       [:p "Written by: " (:author recipe)]]
      [:div.buttons.is-pulled-right
       [:button.button
        "Delete"]
       [:button.button.is-warning
        {:on-click #(rf/dispatch [:common/navigate! :cookbook.routes/edit-recipe {:id (:id recipe)}])}
        "Edit"]]]]))