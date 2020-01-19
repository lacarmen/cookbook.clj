(ns cookbook.users.subscriptions
  (:require [reframe-utils.core :as rf-utils]))

(rf-utils/multi-generation
  rf-utils/reg-basic-sub
  :admin/users
  :admin/user)
