(ns cookbook.routes.spa
  (:require
    [cookbook.layout :as layout]
    [cookbook.middleware :as middleware]
    [ring.util.http-response :as response]))

(defn app-page
  [request]
  (let [identity (dissoc (get-in request [:session :identity])
                         :last-login
                         :created-at
                         :updated-at)]
    (layout/render "home.html" {:identity identity})))

(def spa-route {:get {:handler app-page}})

(def spa-routes
  [""
   {:middleware [#_middleware/wrap-authorized]}
   ["/" spa-route]
   ["/recipe/:id" spa-route]
   ["/recipe/:id/edit" spa-route]])
