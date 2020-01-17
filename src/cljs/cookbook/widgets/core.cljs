(ns cookbook.widgets.core
  (:require
    [re-frame.core :as rf]
    [cookbook.widgets.item-list :as item-list]
    [cookbook.widgets.md-editor :as md-editor]
    [cookbook.widgets.tag-editor :as tag-editor]))

(defn text-input [label path]
  [:div.field
   [:label.label label]
   [:div.control
    {:class (if @(rf/subscribe [:resources/pending?]) "is-loading")}
    [:input.input
     {:type      "text"
      :disabled  @(rf/subscribe [:resources/pending?])
      :value     @(rf/subscribe [:recipe/field path])
      :on-change #(rf/dispatch [:recipe/update path (-> % .-target .-value)])}]]])

(defn tag-input [label path]
  [:div.field
   [:label.label label]
   [tag-editor/tag-input (rf/subscribe [:recipe/field path]) (rf/subscribe [:tags])]])

(defn md-editor [label path]
  [:div.field
   [:label.label label]
   [md-editor/md-editor path]])

(def item-list item-list/item-list)