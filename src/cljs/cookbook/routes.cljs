(ns cookbook.routes
  (:require
    [cookbook.pages.home :refer [home-page]]
    [cookbook.pages.login :refer [login-page]]
    [cookbook.pages.recipe :refer [view-recipe-page edit-recipe-page]]
    [re-frame.core :as rf]
    [reagent.core :as r]
    ["react-beautiful-dnd" :as dnd]
    [reitit.frontend.easy :as rfe]))

(defn nav-link [uri title page]
  [:a.navbar-item
   {:href  uri
    :class (when (= page @(rf/subscribe [:common/page-id])) :is-active)}
   title])

(defn navbar []
  (r/with-let [expanded? (r/atom false)]
    [:nav.navbar.is-warning>div.container
     [:div.navbar-brand
      [:a.navbar-item {:href "/" :style {:font-weight :bold}} "cookbook.clj"]
      [:span.navbar-burger.burger
       {:data-target :nav-menu
        :on-click    #(swap! expanded? not)
        :class       (when @expanded? :is-active)}
       [:span] [:span] [:span]]]
     [:div#nav-menu.navbar-menu
      {:class (when @expanded? :is-active)}
      [:div.navbar-start
       [nav-link "/create" "Create Recipe" :create-recipe]]
      [:div.navbar-end
       [:div.navbar-item
        [:p "Hello, "
         [:a.has-text-dark.has-text-weight-semibold
          (:first-name @(rf/subscribe [:common/user]))]]]
       [:div.navbar-item.has-background-dark.is-paddingless
        {:style {:margin "0.85rem 0" :width "1px"}}]
       [:div.navbar-item
        [:a.has-text-dark.has-text-weight-semibold
         {:on-click #(rf/dispatch [:common/redirect! (rfe/href ::logout)])}
         "Logout"]]]]]))

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
         [login-page])])))

(def routes
  [["/"
    {:name        ::home
     :view        #'home-page
     :title       "Home"
     :controllers [{:start (fn [_]
                             (rf/dispatch [:http/load-recipes])
                             (rf/dispatch [:set-selected-tag nil]))}]}]
   ["/login" {:name        ::login
              :view        #'login-page
              :title       "Login"
              :controllers [{:parameters {:query [:redirect]}
                             :start      (fn [params]
                                           (rf/dispatch [:auth/set-redirect (get-in params [:query :redirect])]))}]}]
   ["/logout" {:name ::logout}]
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
