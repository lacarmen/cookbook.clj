(ns cookbook.routes
  (:require
    [cookbook.pages.home :refer [home-page]]
    [cookbook.pages.recipe :refer [view-recipe-page edit-recipe-page]]
    [re-frame.core :as rf]
    [reagent.core :as r]
    ["react-beautiful-dnd" :as dnd]))

(defn nav-link [uri title page]
  [:a.navbar-item
   {:href  uri
    :class (when (= page @(rf/subscribe [:common/page-id])) :is-active)}
   title])

(defn navbar []
  (r/with-let [expanded? (r/atom false)]
    [:nav.navbar.is-warning>div.container
     [:div.navbar-brand
      [:a.navbar-item {:href "/" :style {:font-weight :bold}} "cookbook"]
      [:span.navbar-burger.burger
       {:data-target :nav-menu
        :on-click    #(swap! expanded? not)
        :class       (when @expanded? :is-active)}
       [:span] [:span] [:span]]]
     [:div#nav-menu.navbar-menu
      {:class (when @expanded? :is-active)}
      [:div.navbar-start
       [nav-link "/create" "Create Recipe" :create-recipe]]]]))

(defn page []
  (if-let [page @(rf/subscribe [:common/page])]
    (if (= ::login @(rf/subscribe [:common/page-id]))
      [page]
      [:div
       [navbar]
       (if (and @(rf/subscribe [:resources/pending?])
                (not @(rf/subscribe [:skip-loading-screen])))
         [:div.pageloader.has-background-warning.is-active
          [:span.title "Loading..."]]
         [page])])))

(def routes
  [["/"
    {:name        ::home
     :view        #'home-page
     :title       "Home"
     :controllers [{:start (fn [_]
                             (rf/dispatch [:http/load-recipes])
                             (rf/dispatch [:set-selected-tag nil]))}]}]
   ["/create"
    {:name        ::create-recipe
     :view        #'edit-recipe-page
     :title       "Recipe"
     :controllers [{:start (fn [_]
                             (rf/dispatch [:http/load-tags])
                             (rf/dispatch [:set-recipe nil]))}]}]
   ["/recipe/:id"
    {:name        ::view-recipe
     :view        #'view-recipe-page
     :title       "Recipe"
     :controllers [{:parameters {:path [:id]}
                    :start      (fn [params]
                                  (rf/dispatch [:http/load-recipe (get-in params [:path :id])]))}]}]
   ["/recipe/:id/edit"
    {:name        ::edit-recipe
     :view        #'edit-recipe-page
     :title       "Edit Recipe"
     :controllers [{:parameters {:path [:id]}
                    :start      (fn [params]
                                  (rf/dispatch [:http/load-recipe (get-in params [:path :id])])
                                  (rf/dispatch [:http/load-tags]))}]}]
   ["/admin"
    {:admin? true}]])