(ns cookbook.pages.login
  (:require
    [re-frame.core :as rf]
    [reagent.core :as r]))

(def key-enter 13)

(defn login-on-enter [user e]
  (condp = (.-keyCode e)
    key-enter (rf/dispatch [:http/login @user])
    :default))

(defn login-page []
  (r/with-let [user (r/atom {})]
    [:section.section>div.container>div.content
     [:div.columns.is-centered
      [:div.column.is-half
       [:h1.title.is-2.has-text-centered.has-text-weight-normal
        [:i.fas.fa-cookie-bite]
        " cookbook.clj"]
       [:p.title.is-4.has-text-centered.has-text-weight-normal
        "Sign in"]
       [:div.field
        [:label.label "Username"]
        [:div.control.has-icons-left
         [:input.input
          {:type        "text"
           :value       (:id @user)
           :on-change   #(swap! user assoc :id (-> % .-target .-value))
           :on-key-down (partial login-on-enter user)}]
         [:span.icon.is-small.is-left
          [:i.fas.fa-user]]]]
       [:div.field
        [:label.label "Password"]
        [:div.control.has-icons-left
         [:input.input
          {:type        "password"
           :value       (:pass @user)
           :on-change   #(swap! user assoc :pass (-> % .-target .-value))
           :on-key-down (partial login-on-enter user)}]
         [:span.icon.is-small.is-left
          [:i.fas.fa-lock]]]]
       [:div.field
        [:div.control
         [:button.button.is-warning.is-fullwidth
          {:on-click #(rf/dispatch [:http/login @user])}
          "Login"]]]]]]))
