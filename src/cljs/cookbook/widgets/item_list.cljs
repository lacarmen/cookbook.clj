(ns cookbook.widgets.item-list
  (:require [re-frame.core :as rf]
            [reagent.core :as r]))

(def key-enter 13)

(defn item-input [path idx]
  (r/create-class
    {:component-did-mount
     (fn [e]
       (let [text-input (some-> e r/dom-node .-childNodes (aget 1) .-firstChild)]
         (rf/dispatch [:run-if-repainting #(.focus text-input)])))
     :reagent-render
     (fn [path idx]
       [:div.field.has-addons
        [:div.control
         [:a.button.is-static
          [:span.icon
           [:i.fas.fa-grip-lines]]]]
        [:div.control.is-expanded
         [:input.input
          {:type        "text"
           :disabled    @(rf/subscribe [:resources/pending?])
           :value       @(rf/subscribe [:recipe/field [path idx]])
           :on-change   #(rf/dispatch [:recipe/update [path idx] (-> % .-target .-value)])
           :on-key-down #(condp = (.-keyCode %)
                           key-enter (rf/dispatch [:recipe/new-list-item path])
                           :default)}]]
        [:div.control
         [:button.button
          {:disabled @(rf/subscribe [:resources/pending?])
           :on-click #(rf/dispatch [:recipe/remove-from-list path idx])}
          [:span.icon
           [:i.fas.fa-trash]]]]])}))

(defn item-list [label path]
  [:div.field.item-list
   [:label.label label]
   (for [idx (range (count @(rf/subscribe [:recipe/field path])))]
     ^{:key [path idx]}
     [item-input path idx])
   [:button.button.is-small
    {:on-click #(rf/dispatch [:recipe/new-list-item path])}
    [:span.icon.is-small
     [:i.fas.fa-plus]]]])
