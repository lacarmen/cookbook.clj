(ns cookbook.events.handlers
  (:require
    [cookbook.events.utils :as utils]
    [ajax.core :as ajax]
    [re-frame.core :as rf]
    [reframe-utils.core :as rf-utils]
    [reitit.frontend.controllers :as rfc]
    [reitit.frontend.easy :as rfe]))

(rf-utils/multi-generation
  rf-utils/reg-set-event
  :common/error
  :common/loading?
  :common/user
  :auth/redirect
  :recipes
  :recipe
  :tags
  :selected-tag)

(rf/reg-event-fx
  :common/init-db
  (fn [_ [_ user]]
    {:dispatch [:common/set-user user]}))

(rf/reg-event-db
  :common/navigate
  (fn [db [_ match]]
    (let [old-match (:common/route db)
          new-match (assoc match :controllers
                                 (rfc/apply-controllers (:controllers old-match) match))]
      (assoc db :common/route new-match))))

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
  :run-if-repainting
  (fn [{{:keys [repainting?] :as db} :db} [_ f]]
    {:db          (dissoc db :repainting?)
     :side-effect #(if repainting? (f))}))

;; recipe stuff

(rf/reg-event-db
  :recipe/update
  (fn [db [_ field value]]
    (assoc-in db
              (utils/conj-flatten [:recipe] field)
              value)))

(rf/reg-event-db
  :recipe/new-list-item
  (fn [db [_ field]]
    (-> db
        (update-in
          (utils/conj-flatten [:recipe] field)
          (fnil conj []) "")
        (assoc :repainting? true))))

(rf/reg-event-db
  :recipe/remove-from-list
  (fn [db [_ field idx]]
    (update-in db
               (utils/conj-flatten [:recipe] field)
               utils/remove-at
               idx)))

;; http stuff
(rf/reg-event-db
  :resources/skip-loading-screen
  (fn [db _]
    (assoc db :skip-loading-screen true)))

(rf/reg-event-db
  :resources/load
  (fn [db [_ resource-id]]
    (update db :pending-resources (fnil conj #{}) resource-id)))

(rf/reg-event-db
  :resources/loaded
  (fn [db [_ resource-id]]
    (-> db
        (update :pending-resources disj resource-id)
        (dissoc :skip-loading-screen))))

(rf/reg-event-fx
  :http/login
  (fn [{:keys [db]} [resource-id user]]
    {:http {:method      ajax/POST
            :url         "/login"
            :resource-id resource-id
            :params      (assoc user :redirect (:auth/redirect db))
            :on-success  [:common/redirect!]}}))

(rf/reg-event-fx
  :http/load-recipes
  (fn [_ [resource-id]]
    {:http {:method      ajax/GET
            :url         "/api/recipes"
            :resource-id resource-id
            :on-success  (fn [recipes]
                           (rf/dispatch [:set-recipes recipes])
                           (rf/dispatch [:set-tags (frequencies (mapcat :tags recipes))]))}}))

(rf/reg-event-fx
  :http/load-recipe
  (fn [_ [resource-id id]]
    {:http {:method      ajax/GET
            :url         (str "/api/recipes/" id)
            :resource-id resource-id
            :on-success  [:set-recipe]}}))

(rf/reg-event-fx
  :http/save-recipe
  (fn [{{{:keys [id] :as recipe} :recipe} :db} [resource-id]]
    {:http {:method               (if id ajax/PUT ajax/POST)
            :url                  "/api/recipes"
            :resource-id          resource-id
            :skip-loading-screen? true
            :params               recipe
            :on-success           [:common/navigate! :cookbook.routes/view-recipe]}}))

(rf/reg-event-fx
  :http/load-tags
  (fn [_ [resource-id]]
    {:http {:method      ajax/GET
            :url         "/api/tags"
            :resource-id resource-id
            :on-success  [:set-tags]}}))
