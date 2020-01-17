(ns cookbook.routes.services.recipes
  (:require
    [cookbook.db.core :as db]
    [clojure.tools.logging :as log]
    [ring.util.http-response :as response]))

(defn get-tags [_]
  (try
    (response/ok (->> (db/get-tags)
                      (mapcat :tags)
                      (frequencies)))
    (catch Exception e
      (log/error "failed to get tags" e)
      (response/internal-server-error {:error "failed to get tags"}))))

(defn get-recipes [_]
  (try
    (response/ok (db/get-recipe-headers))
    (catch Exception e
      (log/error "failed to get recipes" e)
      (response/internal-server-error {:error "failed to get recipes"}))))

(defn get-recipe-by-id [{:keys [parameters]}]
  (try
    (if-let [recipe (db/get-recipe-by-id (:path parameters))]
      (-> recipe
          (dissoc :recipe)
          (merge (:recipe recipe))
          (update :tags vec)
          (response/ok))
      (response/not-found "Recipe not found"))
    (catch Exception e
      (log/error "failed to get recipe" e)
      (response/internal-server-error {:error "failed to get recipe"}))))

(defn- ->recipe [params]
  (let [recipe (select-keys params [:title :description :ingredients :directions])]
    {:id     (:id params)
     :tags   (:tags params)
     :recipe recipe}))

(defn create-recipe! [{:keys [params session]}]
  (try
    (let [author      (select-keys (:identity session) [:id :first-name :last-name])
          recipe      (->recipe params)
          returned-id (db/create-recipe! (assoc recipe :author author))]
      (response/ok returned-id))
    (catch Exception e
      (log/error "failed to create recipe" e)
      (response/internal-server-error {:error "failed to create recipe"}))))

(defn update-recipe! [{:keys [params]}]
  (try
    (let [recipe (->recipe params)]
      (db/update-recipe! recipe)
      (response/ok {:id (:id recipe)}))
    (catch Exception e
      (log/error "failed to udpate recipe" e)
      (response/internal-server-error {:error "failed to udpate recipe"}))))