(ns cookbook.modals
  (:require [re-frame.core :as rf]))

(defmulti render-modal (fn [id args] id))

(defn modal [modal-args]
  (when-let [{:keys [id args]} modal-args]
    (render-modal id args)))

(defn modal-hiccup [id content & [on-close]]
  (let [close-fn (fn []
                   (if on-close (on-close))
                   (rf/dispatch [:modal/close-modal id]))]
    [:div.modal.is-active
     [:div.modal-background
      {:on-click close-fn}]
     [:div.modal-content
      content]
     [:button.modal-close.is-large
      {:aria-label "close"
       :on-click   close-fn}]]))

(defmethod render-modal :default [id args]
  [modal-hiccup id
   [:div.box
    [:p.title.is-5
     (str "Unknown modal id: " (if (keyword? id) (name id) (str id)))]
    [:p.subtitle.is-6 "Args:"]
    [:p (str args)]]])

(defmethod render-modal :error [id error]
  [modal-hiccup id
   [:div.box
    [:p.title.is-5 error]]
   #(rf/dispatch [:common/set-error nil])])

(defmethod render-modal :delete-recipe [id {:keys [delete-fn]}]
  [modal-hiccup id
   [:div.card
    [:div.card-header.has-background-warning
     [:p.card-header-title "Delete recipe"]]
    [:div.card-content
     [:p "Are you sure?"]]
    [:div.card-footer
     [:a.card-footer-item
      {:on-click #(rf/dispatch [:modal/close-modal id])}
      "Cancel"]
     [:a.card-footer-item
      {:on-click delete-fn}
      "Delete"]]]])