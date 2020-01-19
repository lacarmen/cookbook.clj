(ns cookbook.home.subscriptions
  (:require [re-frame.core :as rf]
            [reframe-utils.core :as rf-utils]))

(rf-utils/multi-generation
  rf-utils/reg-basic-sub
  :tags
  [:data/recipes [:recipes]]
  :selected-tag)

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
