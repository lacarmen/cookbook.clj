(ns cookbook.common.events
  (:require
    [cookbook.auth.events]
    [cookbook.home.events]
    [cookbook.profile.events]
    [cookbook.recipe.events]
    [cookbook.users.events]

    [cookbook.utils :as utils]
    [re-frame.core :as rf]
    [reframe-utils.core :as rf-utils]
    [reitit.frontend.controllers :as rfc]
    [reitit.frontend.easy :as rfe]))

(rf-utils/multi-generation
  rf-utils/reg-set-event
  :common/route
  :common/error
  :common/user)

(rf/reg-event-fx
  :common/init-db
  (fn [_ [_ user]]
    {:dispatch [:common/set-user user]}))

(rf/reg-event-fx
  :common/navigate
  (fn [{:keys [db]} [_ match]]
    (let [old-match (:common/route db)
          new-match (assoc match :controllers
                                 (rfc/apply-controllers (:controllers old-match) match))]
      {:db (assoc db :common/route new-match)
       :dispatch [:http/set-loading :page]})))

(rf/reg-fx
  :common/navigate-fx!
  (fn [[k & [params query]]]
    (rfe/push-state k params query)))

(rf/reg-event-fx
  :common/navigate!
  (fn [_ [_ url-key params query]]
    {:common/navigate-fx! [url-key params query]}))

(rf/reg-fx
  :common/redirect-fx!
  (fn [url]
    (.replace (.-location js/window) url)))

(rf/reg-event-fx
  :common/redirect!
  (fn [_ [_ url]]
    {:common/redirect-fx! (or url "/'")}))

(rf/reg-event-db
  :common/ajax-error
  (fn [db _]
    (assoc db :common/error "Error completing the request")))

(rf/reg-event-fx
  :common/run-if-repainting
  (fn [{{:keys [repainting?] :as db} :db} [_ f]]
    {:db          (dissoc db :repainting?)
     :side-effect #(if repainting? (f))}))

;; data access/updates
(rf/reg-event-db
  :data/set-value
  (fn [db [_ path value]]
    (assoc-in db path value)))

(rf/reg-event-db
  :data/new-list-item
  (fn [db [_ path]]
    (-> db
        (update-in
          path
          (fnil conj []) "")
        (assoc :repainting? true))))

(rf/reg-event-db
  :data/remove-from-list
  (fn [db [_ path idx]]
    (update-in db
               path
               utils/remove-at
               idx)))

;; http stuff
(rf/reg-event-db
  :http/skip-loading-screen
  (fn [db _]
    (assoc db :http/skip-loading-screen true)))

(rf/reg-event-db
  :http/set-loading
  (fn [db [_ resource-id]]
    (update db :http/pending-resources (fnil conj #{}) resource-id)))

(rf/reg-event-db
  :http/set-loaded
  (fn [db [_ resource-id]]
    (-> db
        (update :http/pending-resources disj resource-id)
        (update :http/pending-resources disj :page)
        (dissoc :http/skip-loading-screen))))
