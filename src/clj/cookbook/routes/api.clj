(ns cookbook.routes.api
  (:require
    [cookbook.middleware :as middleware]
    [cookbook.routes.services.recipes :as recipes]

    [schema.core :as s]
    [cookbook.routes.services.auth :as auth]))

(def service-routes
  ["/api"
   {:middleware [middleware/wrap-authorized]}
   ["/tags" {:get {:handler recipes/get-tags}}]
   ["/recipes"
    ["" {:get  {:handler recipes/get-recipes}
         :post {:handler recipes/create-recipe!}
         :put  {:handler recipes/update-recipe!}}]
    ["/:id" {:get {:parameters {:path {:id s/Int}}
                   :handler    recipes/get-recipe-by-id}}]]
   ["/profile" {:put {:handler (partial auth/update-user! true)}}]
   ["/admin"
    {:middleware [middleware/wrap-enforce-admin]}
    ["/users"
     ["" {:get  {:handler auth/get-users}
          :post {:handler auth/create-user!}
          :put  {:handler (partial auth/update-user! false)}}]
     ["/:id" {:get {:handler auth/get-user-by-id}}]]]])
