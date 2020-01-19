(ns cookbook.widgets.item-list
  (:require
    [cookbook.utils :as event-utils]
    [re-frame.core :as rf]
    [reagent.core :as r]))

(def key-enter 13)

(defn item-input [path idx]
  (r/create-class
    {:component-did-mount
     (fn [e]
       (let [text-input (some-> e r/dom-node .-childNodes (aget 1) .-firstChild)]
         (rf/dispatch [:common/run-if-repainting #(.focus text-input)])))
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
           :disabled    @(rf/subscribe [:http/loading?])
           :value       @(rf/subscribe [:data/get-value (event-utils/conj-flatten path idx)])
           :on-change   #(rf/dispatch [:data/set-value (event-utils/conj-flatten path idx) (-> % .-target .-value)])
           :on-key-down #(condp = (.-keyCode %)
                           key-enter (rf/dispatch [:data/new-list-item path])
                           :default)}]]
        [:div.control
         [:button.button
          {:disabled @(rf/subscribe [:http/loading?])
           :on-click #(rf/dispatch [:data/remove-from-list path idx])}
          [:span.icon
           [:i.fas.fa-trash]]]]])}))

(defn item-list [label path]
  [:div.field.item-list
   [:label.label label]
   (for [idx (range (count @(rf/subscribe [:data/get-value path])))]
     ^{:key [path idx]}
     [item-input path idx])
   [:button.button.is-small
    {:on-click #(rf/dispatch [:data/new-list-item path])}
    [:span.icon.is-small
     [:i.fas.fa-plus]]]])
