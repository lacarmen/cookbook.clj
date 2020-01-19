(ns cookbook.common.subscriptions
  (:require
    [cookbook.home.subscriptions]
    [cookbook.recipe.subscriptions]
    [cookbook.users.subscriptions]

    [cookbook.utils :as utils]
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
  :common/user
  [:common/admin? [:common/user :admin]]
  :http/skip-loading-screen)

(rf/reg-sub
  :data/get-value
  (fn [db [_ path]]
    (get-in db path)))

(rf/reg-sub
  :http/loading?
  (fn [db _]
    (boolean (not-empty (:http/pending-resources db)))))

(rf/reg-sub
  :modal/display-modal
  (fn [db _]
    (->> (:modals db)
         (sort-by (fn [[_ args]] (:at args)) >)
         (first)
         (second))))

(rf/reg-sub
  :db
  (fn [db _]
    db))