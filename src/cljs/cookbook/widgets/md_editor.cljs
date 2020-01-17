(ns cookbook.widgets.md-editor
  (:require
    [markdown.core :refer [md->html]]
    [re-frame.core :as rf]
    [reagent.core :as r]))

(defn preview [path]
  [:div.md-preview
   {:dangerouslySetInnerHTML {:__html (md->html @(rf/subscribe [:recipe/field path]))}}])

(defn editor [path]
  (let [editor (r/atom nil)]
    (r/create-class
      {:component-did-mount
       (fn [e]
         (-> (reset! editor
                     (js/SimpleMDE.
                       (clj->js
                         {:spellChecker      false
                          :styleSelectedText false
                          :placeholder       "Directions"
                          :element           (r/dom-node e)
                          :initialValue      @(rf/subscribe [:recipe/field path])
                          :insertTexts       {:image ["![](" ")"]
                                              :link  ["[" "]()"]}
                          :toolbar           ["bold" "italic" "|"
                                              "heading" "heading-smaller" "heading-bigger" "|"
                                              "unordered-list" "ordered-list" "|"
                                              "link" "image" "table" "horizontal-rule" "|"
                                              "guide"]})))
             .-codemirror
             (.on "change" #(rf/dispatch [:recipe/update path (.value editor)])))
         (add-watch (rf/subscribe [:resources/pending?])
                    :md-editor
                    (fn [_ _ _ new-state]
                      (if new-state
                        (-> @editor .-codemirror (.setOption "readOnly" true))
                        (-> @editor .-codemirror (.setOption "readOnly" false))))))

       :component-will-unmount
       (fn [_]
         (remove-watch (rf/subscribe [:resources/pending?]) :md-editor)
         (.toTextArea @editor))

       :reagent-render
       (fn [] [:textarea])})))

(defn editor-view-button [view-atom view icon]
  [:div.control
   [:button.button
    {:class    (if (= view @view-atom) "has-background-warning")
     :disabled @(rf/subscribe [:resources/pending?])
     :on-click #(reset! view-atom view)}
    [:span.icon.is-small
     [:i.fas
      {:class (str "fa-" icon)}]]]])

(defn md-editor [path]
  (r/with-let [view (r/atom :split)]
    [:div
     [:div.field.has-addons
      [editor-view-button view :edit "pen"]
      [editor-view-button view :split "columns"]
      [editor-view-button view :preview "file-alt"]]
     (case @view
       :split
       [:div.columns
        [:div.column [editor path]]
        [:div.column [preview path]]]
       :preview
       [preview path]
       :edit
       [editor path])]))
