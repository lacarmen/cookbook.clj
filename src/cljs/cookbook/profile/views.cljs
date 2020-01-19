(ns cookbook.profile.views
  (:require
    [cookbook.widgets.core :as widgets]
    [re-frame.core :as rf]))

(defn get-page-config [page-id]
  (condp = page-id
    :cookbook.routes/profile
    {:base-path        :common/user-update
     :save-event       :http/update-profile
     :is-editing-user? false}
    :cookbook.routes/edit-user
    {:base-path        :admin/user
     :save-event       :http/update-user
     :is-editing-user? true}
    {}))

(defn profile-page []
  (let [{:keys [base-path save-event is-editing-user?]}
        (get-page-config @(rf/subscribe [:common/page-id]))]
    [:section.section>div.container>div.content
     [:div.columns.is-centered
      [:div.column.is-three-quarters
       [:div.box
        [:h2.title.is-4 "Profile Management"]
        [:div.columns
         [:div.column
          [:div.field
           [:label.label "Username"]
           [:input.input
            {:type     "text"
             :disabled true
             :value    @(rf/subscribe [:data/get-value [base-path :id]])}]]]
         [:div.column
          [widgets/input :email "Email" [base-path :email]]]]
        [:div.columns
         [:div.column
          [widgets/input :text "First Name" [base-path :first-name]]]
         [:div.column
          [widgets/input :text "Last Name" [base-path :last-name]]]]
        (when is-editing-user?
          [:div.content
           [widgets/checkbox " Active" [base-path :active]]
           [widgets/checkbox " Admin" [base-path :admin]]])
        [:button.button.is-warning
         {:on-click #(rf/dispatch [save-event])
          :class    (if @(rf/subscribe [:http/loading?]) "is-loading")
          :disabled @(rf/subscribe [:http/loading?])}
         "Update Profile"]
        [:hr]
        [:h2.title.is-4 "Update Password"]
        [:div.columns
         [:div.column
          [widgets/input :password "Password" [base-path :pass]]]
         [:div.column
          [widgets/input :password "Confirm Password" [base-path :confirm-pass]]]]
        [:button.button.is-warning
         {:on-click #(rf/dispatch [save-event])
          :class    (if @(rf/subscribe [:http/loading?]) "is-loading")
          :disabled @(rf/subscribe [:http/loading?])}
         "Update Password"]]]]]))
