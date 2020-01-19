(ns cookbook.home.events
  (:require
    [ajax.core :as ajax]
    [re-frame.core :as rf]
    [reframe-utils.core :as rf-utils]))

(rf-utils/multi-generation
  rf-utils/reg-set-event
  :recipes
  :tags
  :selected-tag)

(rf/reg-event-fx
  :http/load-recipes
  (fn [_ [resource-id]]
    {:http {:method      ajax/GET
            :url         "/api/recipes"
            :resource-id resource-id
            :on-success  (fn [recipes]
                           (rf/dispatch [:set-recipes recipes])
                           (rf/dispatch [:set-tags (frequencies (mapcat :tags recipes))]))}}))


;; page controllers
(rf/reg-event-fx
  :page/init-home
  (fn [_ _]
    {:dispatch-n [[:http/load-recipes]
                  [:set-selected-tag nil]]}))