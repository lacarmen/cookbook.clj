(ns cookbook.users.views
  (:require
    ["moment" :as moment]
    [re-frame.core :as rf]
    [cookbook.widgets.core :as widgets]
    [reagent.core :as r]))

(defn user-row [{:keys [id first-name last-name active admin last-login] :as user}]
  (r/with-let [hovered? (r/atom false)]
    [:tr
     {:style          {:cursor :pointer}
      :on-click       #(rf/dispatch [:common/navigate! :cookbook.routes/edit-user {:id id}])
      :on-mouse-over  #(reset! hovered? true)
      :on-mouse-leave #(reset! hovered? false)}
     [:td
      {:width "350px"}
      [:div.content
       [:p.title.is-5
        (str first-name " " last-name)]
       [:p.subtitle.is-6
        (str "@" id " | last login: "
             (if last-login
               (.format (moment last-login) "MMM DD, YYYY - HH:mm")
               "never"))]]]
     [:td
      {:style {:vertical-align :middle}}
      [:span
       (if active
         [:i.fas.fa-check.has-text-success]
         [:i.fas.fa-times.has-text-danger]) " active"]
      [:span
       {:style {:margin-left "30px"}}
       (if admin
         [:i.fas.fa-check.has-text-success]
         [:i.fas.fa-times.has-text-danger]) " admin"]]
     [:td
      {:width "50px"
       :style {:vertical-align :middle}}
      (if @hovered? [:i.fas.fa-edit.is-pulled-right])]]))

(defn create-user-section [create-user?]
  [:div.box
   [:h2.title.is-5 "Add User"]
   [:div.columns
    [:div.column
     [widgets/input :text "Username" [:admin/user :id]]]
    [:div.column
     [widgets/input :email "Email" [:admin/user :email]]]]
   [:div.columns
    [:div.column
     [widgets/input :text "First Name" [:admin/user :first-name]]]
    [:div.column
     [widgets/input :text "Last Name" [:admin/user :last-name]]]]
   [:hr]
   [:div.columns
    [:div.column
     [widgets/input :password "Password" [:admin/user :pass]]]
    [:div.column
     [widgets/input :password "Confirm Password" [:admin/user :confirm-pass]]]]
   [:hr]
   [widgets/checkbox " Admin" [:admin/user :admin]]
   [:div.buttons
    [:button.button.is-small
     {:on-click #(reset! create-user? false)}
     "Cancel"]
    [:button.button.is-warning.is-small
     {:on-click (fn [] (rf/dispatch [:http/create-user #(reset! create-user? false)]))
      :class    (if @(rf/subscribe [:http/loading?]) "is-loading")
      :disabled @(rf/subscribe [:http/loading?])}
     "Create User"]]])

(defn users-page []
  (r/with-let [create-user? (r/atom false)]
    [:section.section>div.container>div.content
     [:div.columns.is-centered
      [:div.column.is-three-quarters
       [:div.box
        [:h2.title.is-4 "User Management  "
         (if-not @create-user?
           [:button.button.is-warning.is-small
            {:on-click #(reset! create-user? true)}
            "Add User"])]
        (when @create-user?
          [create-user-section create-user?])
        [:table.table
         [:tbody
          (for [user @(rf/subscribe [:admin/users])]
            ^{:key (:id user)}
            [user-row user])]]]]]]))