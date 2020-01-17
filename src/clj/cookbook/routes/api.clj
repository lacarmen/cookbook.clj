(ns cookbook.routes.api
  (:require
    [cookbook.middleware :as middleware]
    [cookbook.routes.services.recipes :as recipes]

    [schema.core :as s]))

(def service-routes
  ["/api"
   {#_#_:middleware [middleware/wrap-authorized
                 middleware/wrap-enforce-admin]}
   ["/tags" {:get {:handler recipes/get-tags}}]
   ["/recipes"
    ["" {:get  {:handler recipes/get-recipes}
         :post {:handler recipes/create-recipe!}
         :put  {:handler recipes/update-recipe!}}]
    ["/:id" {:get {:parameters {:path {:id s/Int}}
                   :handler    recipes/get-recipe-by-id}}]]])
