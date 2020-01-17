(ns cookbook.validation
  (:require
    [bouncer.core :as b]
    [bouncer.validators :as v]))

(defn format-validation-errors [errors]
  (->> errors
       first
       (map (fn [[k [v]]] [k v]))
       (into {})
       not-empty))

(defn pass-matches? [pass-confirm pass]
  (= pass pass-confirm))

(defn validate-create-user [user]
  (format-validation-errors
    (b/validate
      (fn [{:keys [path]}]
        ({[:id]           "ID is required"
          [:pass]         "Password of 8+ characters is required"
          [:pass-confirm] "Password confirmation doesn't match"
          [:is-admin]     "You must specify whether the user is an admin"}
         path))
      user
      :id v/required
      :pass [v/required [v/min-count 8]]
      :pass-confirm [[pass-matches? (:pass user)]]
      :admin v/required)))

(defn validate-update-user [user]
  (format-validation-errors
    (b/validate
      (fn [{:keys [path]}]
        ({[:id]           ID
          [:is-admin]     "You must specify whether the user is an admin"
          [:pass-confirm] "Password confirmation doesn't match"
          [:active]       "You must specify whether the user is active"}
         path))
      user
      :id v/required
      :pass-confirm [[pass-matches? (:pass user)]]
      :admin v/required
      :is-active v/required)))