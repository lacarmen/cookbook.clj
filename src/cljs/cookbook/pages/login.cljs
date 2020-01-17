(ns cookbook.pages.login
  (:require
    [re-frame.core :as rf]
    [reagent.core :as r]))

[:form {:method "POST" :action "https://pingstack.io/app/login"}
 [:input {:type "hidden" :name "_token" :value "CH8hVQHyoRl98Kz7c3Lu4tu9J13g36sDucZkMEzl"}]
 [:div.form-group
  [:label "Email Address"]
  [:input#email.form-control {:type "email" :name "email" :value "" :required "" :autofocus ""}]]
 [:div.form-group
  [:label "Password"]
  [:div.input-group.input-group-merge
   [:input#password.form-control {:type "password" :name "password" :required ""}]]]
 [:div.form-group
  [:div.form-check
   [:input#remember.form-check-input {:type "checkbox" :name "remember"}]
   [:label.form-check-label {:for "remember"} "Remember me"]]]
 [:button.btn.btn-lg.btn-block.btn-primary.mb-3 {:type "submit"} "Log in"]
 ]

(defn login-page []
  (r/with-let [user (r/atom {})]
    [:section.section>div.container>div.content
     [:div.field
      [:div.control.has-icons-left
       [:input.input
        {:type        "text"
         :placeholder "Username"
         :value       (:id @user)
         :on-change #(swap! user assoc :id (-> % .-target .-value))}]
       [:span.icon.is-small.is-left
        [:i.fas.fa-user]]]]
     [:div.field
      [:div.control.has-icons-left
       [:input.input
        {:type        "password"
         :placeholder "Password"
         :value       (:pass @user)
         :on-change   #(swap! user assoc :pass (-> % .-target .-value))}]
       [:span.icon.is-small.is-left
        [:i.fas.fa-lock]]]]
     [:div.field
      [:div.control
       [:button.button.is-warning.is-fullwidth
        {:on-click #(println @user)}
        "Login"]]]]))
