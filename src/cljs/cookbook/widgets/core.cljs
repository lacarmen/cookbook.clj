(ns cookbook.widgets.core
  (:require
    [re-frame.core :as rf]
    [cookbook.widgets.item-list :as item-list]
    [cookbook.widgets.md-editor :as md-editor]
    [cookbook.widgets.tag-editor :as tag-editor]))

(defn checkbox [label path]
  (let [value @(rf/subscribe [:data/get-value path])]
    [:div.field>div.control
     [:label.checkbox
      [:input
       {:type      "checkbox"
        :disabled  @(rf/subscribe [:http/loading?])
        :checked   value
        :on-change #(rf/dispatch [:data/set-value path (not value)])}]
      label]]))

(defn input [type label path]
  [:div.field
   [:label.label label]
   [:div.control
    {:class (if @(rf/subscribe [:http/loading?]) "is-loading")}
    [:input.input
     {:type      type
      :disabled  @(rf/subscribe [:http/loading?])
      :value     @(rf/subscribe [:data/get-value path])
      :on-change #(rf/dispatch [:data/set-value path (-> % .-target .-value)])}]]])

(defn tag-input [label path]
  [:div.field
   [:label.label label]
   [tag-editor/tag-input path (rf/subscribe [:tags])]])

(defn md-editor [label path]
  [:div.field
   [:label.label label]
   [md-editor/md-editor path]])

(def item-list item-list/item-list)