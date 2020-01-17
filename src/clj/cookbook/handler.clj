(ns cookbook.handler
  (:require
    [cookbook.env :refer [defaults]]
    [cookbook.layout :refer [error-page]]
    [cookbook.middleware :as middleware]
    [cookbook.routes.api :as api]
    [cookbook.routes.public :as public]
    [cookbook.routes.spa :as spa]

    [mount.core :as mount]
    [reitit.coercion.schema :as schema-coercion]
    [reitit.ring :as ring]
    [reitit.ring.coercion :as rrc]
    [ring.middleware.content-type :refer [wrap-content-type]]
    [ring.middleware.webjars :refer [wrap-webjars]]))

(mount/defstate init-app
  :start ((or (:init defaults) (fn [])))
  :stop ((or (:stop defaults) (fn []))))

(mount/defstate app-routes
  :start
  (ring/ring-handler
    (ring/router
      [public/public-routes
       spa/spa-routes
       api/service-routes]
      {:data {:middleware [middleware/wrap-csrf
                           middleware/wrap-formats
                           rrc/coerce-exceptions-middleware
                           rrc/coerce-request-middleware
                           rrc/coerce-response-middleware]
              :coercion   schema-coercion/coercion}})
    (ring/routes
      (ring/create-resource-handler
        {:path "/"})
      (wrap-content-type
        (wrap-webjars (constantly nil)))
      (ring/create-default-handler
        {:not-found
         (constantly (error-page {:status 404, :title "404 - Page not found"}))
         :method-not-allowed
         (constantly (error-page {:status 405, :title "405 - Not allowed"}))
         :not-acceptable
         (constantly (error-page {:status 406, :title "406 - Not acceptable"}))}))))

(defn app []
  (middleware/wrap-base #'app-routes))

(comment
  (do
    (mount/stop #'app-routes)
    (mount/start #'app-routes))
  )