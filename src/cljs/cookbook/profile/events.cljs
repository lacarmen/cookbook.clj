(ns cookbook.profile.events
  (:require
    [ajax.core :as ajax]
    [re-frame.core :as rf]))

(rf/reg-event-fx
  :http/update-profile
  (fn [{:keys [db]} [resource-id]]
    (let [user (:profile/user db)]
      {:http {:method               ajax/PUT
              :url                  "/api/profile"
              :resource-id          resource-id
              :skip-loading-screen? true
              :params               user
              :on-success           #(rf/dispatch [:common/set-user (dissoc user :pass :confirm-pass)])}})))

;; page controllers
(rf/reg-event-fx
  :page/init-profile
  (fn [{:keys [db]} _]
    {:db (assoc db :profile/user (:common/user db))}))

(rf/reg-event-fx
  :page/destroy-profile
  (fn [{:keys [db]} _]
    {:db (dissoc db :profile/user)}))
