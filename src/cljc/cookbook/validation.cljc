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
          [:first-name]   "First name is required"
          [:last--name]   "Last name is required"
          [:pass]         "Password of 8+ characters is required"
          [:pass-confirm] "Password confirmation doesn't match"}
         path))
      user
      :id v/required
      :first-name v/required
      :last-name v/required
      :pass [v/required [v/min-count 8]]
      :confirm-pass [[pass-matches? (:pass user)]])))

(defn validate-update-user [user]
  (format-validation-errors
    (b/validate
      (fn [{:keys [path]}]
        ({[:id]           "ID is required"
          [:pass-confirm] "Password confirmation doesn't match"
          [:active]       "You must specify whether the user is active"
          [:admin]        "You must specify whether the user is an admin"}
         path))
      user
      :id v/required
      :confirm-pass [[pass-matches? (:pass user)]]
      :active v/required
      :admin v/required)))
