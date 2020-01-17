(ns cookbook.routes.services.auth
  (:require
    [cookbook.db.core :as db]
    [cookbook.validation :as v]
    [buddy.hashers :as hashers]
    [clojure.tools.logging :as log]
    [ring.util.http-response :as response]
    [conman.core :as conman])
  (:import (java.time LocalDateTime)))

(comment (db/create-user!
           {:id "carmen" :first_name "Carmen", :last_name "La", :email "carmen.wla@gmail.com", :admin true, :pass (hashers/encrypt "foobar")}))


(defn create-user! [user]
  (if-let [errors (v/validate-create-user user)]
    (do
      (log/error "error creating user:" errors)
      (response/bad-request {:error "invalid user"}))
    (try
      (response/ok
        (db/create-user! (update user :pass hashers/encrypt)))
      (catch Exception e
        (log/error "failed to create user" e)
        (response/internal-server-error {:error "failed to create user"})))))

(defn update-user! [{:keys [pass] :as user}]
  (if-let [errors (v/validate-update-user user)]
    (do
      (log/error "error updating user:" errors)
      (response/bad-request {:error "invalid user"}))
    (try
      (response/ok
        (if pass
          (db/update-user-with-pass! (update user :pass hashers/encrypt))
          (db/update-user! user)))
      (catch Exception e
        (log/error "failed to update user" e)
        (response/internal-server-error {:error "failed to update user"})))))

(defn- authenticate!
  "Returns the user if authentication was successful, nil otherwise"
  [id pass]
  (conman/with-transaction [db/*db*]
    (when-let [user (db/get-user-by-id {:id id})]
      (when (hashers/check pass (:pass user))
        (db/update-user-last-login! {:id id})
        (-> user
            (dissoc :pass)
            (assoc :last-login (LocalDateTime/now)))))))


(defn login! [{:keys [session params] :as req}]
  (if-let [user (authenticated-user (decode-auth (auth req)))]
    (-> params
        :redirect
        response
        (assoc :session (assoc session :identity user)))
    (http/unauthorized {:error "invalid login"})))

(defn login! [{{:keys [id pass redirect]} :params session :session}]
  (if-let [user (authenticate! id pass)]
    (do
      (log/info "user:" id "logged in" )
      (-> (response/ok (dissoc user :pass))
          (assoc :session (assoc session :identity user))))
    (do
      (log/info "login failed for" id)
      (response/unauthorized {:error "The username or password was incorrect."}))))

(defn logout! [_]
  (assoc (response/ok) :session nil))
