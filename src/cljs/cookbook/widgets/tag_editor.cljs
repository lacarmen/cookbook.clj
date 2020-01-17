(ns cookbook.widgets.tag-editor
  (:require
    [clojure.set :refer [difference]]
    [clojure.string :as string]
    [reagent.core :as r]
    [re-frame.core :as rf]))

(def key-up 38)
(def key-down 40)
(def key-enter 13)
(def key-escape 27)
(def key-backspace 8)

(defn- close-typeahead [state]
  (swap! state
         merge
         {:selected-index    -1
          :typeahead-hidden? true
          :mouse-on-list?    false}))

(defn- scroll-target-list [event idx]
  (when (> idx -1)
    (let [ul (-> event .-target .-nextSibling)]
      (when-let [element (aget (.getElementsByTagName ul "li") idx)]
        (set! (.-scrollTop ul) (.-offsetTop element))))))

(defn- filter-selections [all-tags recipe-tags state]
  (if-let [user-input (not-empty @(r/cursor state [:user-input]))]
    (swap! state assoc :selections (->> (difference all-tags recipe-tags)
                                        (sort)
                                        (filter #(string/includes? (string/lower-case %)
                                                                   (some-> user-input string/lower-case)))))
    (swap! state assoc :selections nil)))

(defn- remove-tag [tags tag]
  (->> tags
       (remove #(= % tag))
       vec))

(defn- add-tag-and-close-typeahead
  [recipe-tags-rx state & [new-tag]]
  (let [new-tag (some-> (or new-tag @(r/cursor state [:user-input]))
                        string/trim
                        not-empty)]
    (when new-tag
      (rf/dispatch [:recipe/update :tags (conj (vec @recipe-tags-rx) new-tag)])
      (swap! state assoc :user-input nil)
      (close-typeahead state))))

(defn- typeahead-item [item idx recipe-tags-rx state]
  (let [selected-index (r/cursor state [:selected-index])]
    [:a.dropdown-item
     {:class         (when (= @selected-index idx) "is-active")
      :on-mouse-over #(reset! selected-index (js/parseInt (.getAttribute (.-target %) "tabIndex")))
      :on-click      #(add-tag-and-close-typeahead recipe-tags-rx state item)}
     item]))

(defn- typeahead-list [recipe-tags-rx state]
  (let [{:keys [selections typeahead-hidden?]} @state
        mouse-on-list? (r/cursor state [:mouse-on-list?])]
    [:div
     {:style {:display (if (or (empty? selections) typeahead-hidden?) :none :block)}}
     [:div.dropdown-menu
      {:style          {:display :block}
       :on-mouse-enter #(reset! mouse-on-list? true)
       :on-mouse-leave #(reset! mouse-on-list? false)}
      [:div.dropdown-content
       (for [[idx item] (map-indexed vector selections)]
         ^{:key idx}
         [typeahead-item item idx recipe-tags-rx state])]]]))

(defn- tag-list [recipe-tags-rx all-tags]
  (let [new-tags (-> (set @recipe-tags-rx)
                     (difference all-tags)
                     (not-empty))]
    (into
      [:div.tags.is-marginless]
      (for [tag @recipe-tags-rx]
        ^{:key tag}
        [:span.tag
         {:class (if (contains? new-tags tag) "is-default" "is-warning")}
         tag
         [:button.delete.is-small
          {:on-click #(rf/dispatch [:recipe/update :tags (remove-tag @recipe-tags-rx tag)])}]]))))

(def default-state
  {:typeahead-hidden? true
   :mouse-on-list?    false
   :selected-index    -1
   :selections        []
   :user-input        nil})

(defn tag-input [recipe-tags-rx all-tags-rx]
  (r/with-let
    [all-tags          (set (keys @all-tags-rx))
     state             (r/atom default-state)
     typeahead-hidden? (r/cursor state [:typeahead-hidden?])
     mouse-on-list?    (r/cursor state [:mouse-on-list?])
     selected-index    (r/cursor state [:selected-index])
     selections        (r/cursor state [:selections])
     user-input        (r/cursor state [:user-input])
     choose-selected   #(add-tag-and-close-typeahead recipe-tags-rx state (get (vec @selections) @selected-index))]
    [:div.tags-input
     [tag-list recipe-tags-rx all-tags]
     [:div.control
      {:class (if @(rf/subscribe [:resources/pending?]) "is-loading")}]
     [:input.input
      {:type        :text

       :disabled    @(rf/subscribe [:resources/pending?])

       :placeholder "Type tag and press enter to add"

       :value       @user-input

       :on-focus    #(do
                       (reset! typeahead-hidden? false)
                       (if (seq @user-input)
                         (filter-selections all-tags (set @recipe-tags-rx) state)
                         (reset! selections [])))
       :on-change   #(do
                       (reset! user-input (-> % .-target .-value))
                       (filter-selections all-tags (set @recipe-tags-rx) state)
                       (reset! typeahead-hidden? false)
                       (reset! selected-index -1))

       :on-blur     #(when-not @mouse-on-list?
                       (close-typeahead state))

       :on-key-down #(condp = (.-keyCode %)
                       key-up (do
                                (.preventDefault %)
                                (when-not (neg? @selected-index)
                                  (swap! selected-index dec)
                                  (scroll-target-list % @selected-index)))
                       key-down (do
                                  (.preventDefault %)
                                  (when-not (= @selected-index (dec (count @selections)))
                                    (swap! selected-index inc)
                                    (scroll-target-list % @selected-index)))
                       key-enter (if (neg? @selected-index)
                                   (add-tag-and-close-typeahead recipe-tags-rx state)
                                   (choose-selected))
                       key-escape (close-typeahead state)
                       "Default")}]
     [typeahead-list recipe-tags-rx state]]))
