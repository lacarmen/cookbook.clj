(ns cookbook.routes.public
  (:require
    [cookbook.layout :as layout]
    [cookbook.routes.services.auth :as auth]
    [ring.util.http-response :as response]))

(def public-routes
  [["/login" {:get  (fn [request]
                      (if (get-in request [:session :identity])
                        (response/found (or (get-in request [:query :redirect]) "/"))
                        (layout/render "home.html")))
              :post auth/login!}]
   ["/logout" {:post auth/logout!}]])
