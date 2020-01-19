(ns cookbook.users.events
  (:require
    [ajax.core :as ajax]
    [re-frame.core :as rf]
    [reframe-utils.core :as rf-utils]))

(rf-utils/multi-generation
  rf-utils/reg-set-event
  :admin/users
  :admin/user)

(rf/reg-event-fx
  :http/load-users
  (fn [_ [resource-id]]
    {:http {:method      ajax/GET
            :url         "/api/admin/users"
            :resource-id resource-id
            :on-success  [:admin/set-users]}}))

(rf/reg-event-fx
  :http/load-user
  (fn [_ [resource-id id]]
    {:http {:method      ajax/GET
            :url         (str "/api/admin/users/" id)
            :resource-id resource-id
            :on-success  [:admin/set-user]}}))

(rf/reg-event-fx
  :http/update-user
  (fn [{:keys [db]} [resource-id]]
    (let [user (:admin/user db)]
      {:http {:method               ajax/PUT
              :url                  "/api/admin/users"
              :resource-id          resource-id
              :skip-loading-screen? true
              :params               user
              :on-success           #(rf/dispatch [:common/navigate! :cookbook.routes/users])}})))

(rf/reg-event-fx
  :http/create-user
  (fn [{:keys [db]} [resource-id on-success]]
    (let [user (:admin/user db)
          users (:admin/users db)]
      {:http {:method               ajax/POST
              :url                  "/api/admin/users"
              :resource-id          resource-id
              :skip-loading-screen? true
              :params               user
              :on-success           #(do (on-success)
                                         (rf/dispatch [:admin/set-users (conj users user)])
                                         (rf/dispatch [:admin/set-user nil]))}})))

;; page controllers

(rf/reg-event-fx
  :page/init-users
  (fn [_ _]
    {:dispatch [:http/load-users]}))

(rf/reg-event-fx
  :page/init-edit-user
  (fn [_ [_ id]]
    {:dispatch [:http/load-user id]}))

(rf/reg-event-fx
  :page/destroy-edit-user
  (fn [{:keys [db]} _]
    {:db (dissoc db :admin/user)}))