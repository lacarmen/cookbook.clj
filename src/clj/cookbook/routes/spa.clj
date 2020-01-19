(ns cookbook.routes.spa
  (:require
    [cookbook.layout :as layout]
    [cookbook.middleware :as middleware]))

(defn app-page
  [request]
  (layout/render
    "home.html"
    {:identity (get-in request [:session :identity])}))

(def spa-route {:get {:handler app-page}})

(def spa-routes
  [""
   {:middleware [middleware/wrap-authorized]}
   ["/" spa-route]
   ["/recipe/:id" spa-route]
   ["/recipe/:id/edit" spa-route]
   ["/create-recipe" spa-route]
   ["/profile" spa-route]
   ["/users"
    {:middleware [middleware/wrap-enforce-admin]}
    ["" spa-route]
    ["/:id" spa-route]]])
