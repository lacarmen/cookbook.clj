(ns cookbook.routes.services.auth
  (:require
    [cookbook.db.core :as db]
    [cookbook.validation :as v]
    [buddy.hashers :as hashers]
    [clojure.tools.logging :as log]
    [ring.util.http-response :as response]
    [conman.core :as conman]))

(defn get-users [{session :session}]
  (try
    (response/ok (db/get-users {:id (get-in session [:identity :id])}))
    (catch Exception e
      (log/error "failed to get users" e)
      (response/internal-server-error {:error "failed to get users"}))))

(defn get-user-by-id [{:keys [path-params]}]
  (try
    (if-let [user (db/get-user-by-id {:id (:id path-params)})]
      (response/ok (dissoc user :pass :last-login))
      (response/not-found "User not found"))
    (catch Exception e
      (log/error "failed to get user" e)
      (response/internal-server-error {:error "failed to get user"}))))

(defn create-user! [{user :params}]
  (let [user (merge {:email nil :admin false} user)]
    (if-let [errors (v/validate-create-user user)]
      (do
        (log/error "error creating user:" errors)
        (response/bad-request {:error "invalid user"}))
      (try
        (response/ok
          (db/create-user! (update user :pass hashers/encrypt)))
        (catch Exception e
          (log/error "failed to create user" e)
          (response/internal-server-error {:error "failed to create user"}))))))

(defn update-user! [update-session? {{:keys [id pass] :as user} :params session :session}]
  (if-let [errors (v/validate-update-user user)]
    (do
      (log/error "error updating user:" errors)
      (response/bad-request {:error "invalid user"}))
    (try
      (if pass
        (db/update-user-with-pass! (update user :pass hashers/encrypt))
        (db/update-user! user))
      (if update-session?
        (-> (response/ok)
            (assoc :session (assoc session :identity (dissoc user :pass :confirm-pass))))
        (response/ok))
      (catch Exception e
        (log/error "failed to update user" e)
        (response/internal-server-error {:error "failed to update user"})))))

(defn- authenticate!
  "Returns the user if authentication was successful, nil otherwise"
  [id pass]
  (conman/with-transaction [db/*db*]
    (when-let [user (db/get-user-by-id {:id id})]
      (when (:active user)
        (when (hashers/check pass (:pass user))
          (db/update-user-last-login! {:id id})
          (-> user
              (dissoc :pass)))))))

(defn login! [{{:keys [id pass redirect]} :params session :session}]
  (if-let [user (authenticate! id pass)]
    (do
      (log/info "user:" id "logged in")
      (-> (response/ok redirect)
          (assoc :session (assoc session :identity user))))
    (do
      (log/info "login failed for" id)
      (response/unauthorized {:error "The username or password was incorrect."}))))

(defn logout! [{:keys [session]}]
  (-> (response/found "/login")
      (assoc :session (dissoc session :identity))))
