(ns cookbook.recipe.events
  (:require
    [ajax.core :as ajax]
    [re-frame.core :as rf]
    [reframe-utils.core :as rf-utils]))

(rf-utils/reg-set-event :recipe)

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


;; page controllers
(rf/reg-event-fx
  :page/init-view-recipe
  (fn [_ [_ id]]
    {:dispatch [:http/load-recipe id]}))

(rf/reg-event-fx
  :page/init-edit-recipe
  (fn [_ [_ id]]
    {:dispatch-n [[:http/load-recipe id]
                  [:http/load-tags]]}))

(rf/reg-event-fx
  :page/init-create-recipe
  (fn [{:keys [db]} _]
    {:dispatch [:http/load-tags]
     :db       (dissoc db :recipe)}))