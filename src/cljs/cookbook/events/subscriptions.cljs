(ns cookbook.events.subscriptions
  (:require
    [cookbook.events.utils :as utils]
    [reframe-utils.core :as rf-utils]
    [re-frame.core :as rf]))

(rf-utils/multi-generation
  rf-utils/reg-basic-sub
  :common/error
  :common/route
  :common/route-params [:common/route :parameter]
  [:common/page [:common/route :data :view]]
  [:common/page-title [:common/route :data :title]]
  [:common/page-id [:common/route :data :name]]
  :common/loading?
  :common/user
  :tags
  [:data/recipes [:recipes]]
  :recipe
  :selected-tag
  :skip-loading-screen)

(rf/reg-sub
  :recipes
  :<- [:data/recipes]
  :<- [:selected-tag]
  (fn [[recipes selected-tag]]
    (if selected-tag
      (filter
        (fn [recipe]
          (some #{selected-tag} (:tags recipe)))
        recipes)
      recipes)))

(rf/reg-sub
  :recipe/field
  (fn [db [_ field]]
    (get-in db (utils/conj-flatten [:recipe] field))))

(rf/reg-sub
  :resources/pending?
  (fn [db _]
    (boolean (not-empty (:pending-resources db)))))

(rf/reg-sub
  :db
  (fn [db _]
    db))