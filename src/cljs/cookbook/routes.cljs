(ns cookbook.routes
  (:require
    ;; pages
    [cookbook.auth.views :refer [login-page]]
    [cookbook.home.views :refer [home-page]]
    [cookbook.profile.views :refer [profile-page]]
    [cookbook.recipe.views :refer [view-recipe-page edit-recipe-page]]
    [cookbook.users.views :refer [users-page]]
    ;;
    [cookbook.modals :as modals]
    ;;
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
       [nav-link "/create-recipe" "Create Recipe" :create-recipe]
       (when @(rf/subscribe [:common/admin?])
         [nav-link "/users" "User Management" :users])]
      [:div.navbar-end
       [:div.navbar-item
        [:p "Hello, "
         [:a.has-text-dark.has-text-weight-semibold
          {:on-click #(rf/dispatch [:common/navigate! ::profile])}
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
       (when-let [display-modal @(rf/subscribe [:modal/display-modal])]
         [modals/modal display-modal])
       (when-let [error @(rf/subscribe [:common/error])]
         [modals/modal {:id :error :args error}])
       (if (and @(rf/subscribe [:http/loading?])
                (not @(rf/subscribe [:http/skip-loading-screen])))
         [:div.pageloader.has-background-warning.is-active
          [:span.title "Loading..."]]
         [page])])))

(def routes
  [["/"
    {:name        ::home
     :view        #'home-page
     :title       "Home"
     :controllers [{:start (fn [_] (rf/dispatch [:page/init-home]))}]}]
   ["/login" {:name        ::login
              :view        #'login-page
              :title       "Login"
              :controllers [{:parameters {:query [:redirect]}
                             :start      (fn [params]
                                           (rf/dispatch [:page/init-login (get-in params [:query :redirect])]))}]}]
   ["/logout" {:name ::logout}]
   ["/recipe"
    ["/:id"
     {:name        ::view-recipe
      :view        #'view-recipe-page
      :title       "Recipe"
      :controllers [{:parameters {:path [:id]}
                     :start      (fn [params]
                                   (rf/dispatch [:page/init-view-recipe (get-in params [:path :id])]))}]}]
    ["/:id/edit"
     {:name        ::edit-recipe
      :view        #'edit-recipe-page
      :title       "Edit Recipe"
      :controllers [{:parameters {:path [:id]}
                     :start      (fn [params]
                                   (rf/dispatch [:page/init-edit-recipe (get-in params [:path :id])]))}]}]]
   ["/create-recipe"
    {:name        ::create-recipe
     :view        #'edit-recipe-page
     :title       "Recipe"
     :controllers [{:start (fn [_]
                             (rf/dispatch [:page/init-create-recipe]))}]}]
   ["/profile"
    {:name        ::profile
     :view        #'profile-page
     :title       "Profile"
     :controllers [{:start (fn [_] (rf/dispatch [:page/init-profile]))
                    :stop  (fn [_] (rf/dispatch [:page/destroy-profile]))}]}]
   ["/users"
    ["" {:name        ::users
         :view        #'users-page
         :title       "Users"
         :controllers [{:start (fn [_] (rf/dispatch [:page/init-users]))}]}]
    ["/:id" {:name        ::edit-user
              :view        #'profile-page
              :title       "Edit User"
              :controllers [{:parameters {:path [:id]}
                             :start (fn [params]
                                      (rf/dispatch [:page/init-edit-user (get-in params [:path :id])]))
                             :stop (fn [_] (rf/dispatch [:page/destroy-edit-user]))}]}]]])
