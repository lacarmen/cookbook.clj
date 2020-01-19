(ns cookbook.auth.events
  (:require
    [ajax.core :as ajax]
    [re-frame.core :as rf]))

(rf/reg-event-fx
  :http/login
  (fn [{:keys [db]} [resource-id user]]
    {:http {:method      ajax/POST
            :url         "/login"
            :resource-id resource-id
            :params      (assoc user :redirect (:auth/redirect db))
            :on-success  [:common/redirect!]}}))


;; page controllers
(rf/reg-event-fx
  :page/init-login
  (fn [{:keys [db]} [_ redirect]]
    {:db (assoc db :auth/redirect redirect)}))