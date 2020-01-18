(ns cookbook.middleware
  (:require
    [cookbook.config :refer [env]]
    [cookbook.env :refer [defaults]]
    [cookbook.layout :refer [error-page]]
    [cookbook.middleware.formats :as formats]
    ;;
    [clojure.tools.logging :as log]
    [immutant.web.middleware :refer [wrap-session]]
    [muuntaja.middleware :refer [wrap-format wrap-params]]
    [ring-ttl-session.core :refer [ttl-memory-store]]
    [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
    [ring.middleware.defaults :refer [site-defaults wrap-defaults]]))

(defn wrap-internal-error [handler]
  (fn [req]
    (try
      (handler req)
      (catch Throwable t
        (log/error t (.getMessage t))
        (error-page {:status 500
                     :title "Something very bad has happened!"
                     :message "We've dispatched a team of highly trained gnomes to take care of the problem."})))))

(defn wrap-csrf [handler]
  (wrap-anti-forgery
    handler
    {:error-response
     (error-page
       {:status 403
        :title "Invalid anti-forgery token"})}))


(defn wrap-formats [handler]
  (let [wrapped (-> handler wrap-params (wrap-format formats/instance))]
    (fn [request]
      ;; disable wrap-formats for websockets
      ;; since they're not compatible with this middleware
      ((if (:websocket? request) handler wrapped) request))))

(defn wrap-base [handler]
  (-> ((:middleware defaults) handler)
      (wrap-session {:cookie-attrs {:http-only true}})
      (wrap-defaults
        (-> site-defaults
            (assoc-in [:security :anti-forgery] false)
            (assoc-in  [:session :store] (ttl-memory-store (* 60 30)))))
      wrap-internal-error))

(defn wrap-enforce-admin [handler]
  (fn [{:keys [session] :as request}]
    (let [admin? (some-> (get-in session [:identity :admin]))]
      (if (not admin?)
        (error-page
          {:status  403
           :title   "Forbidden"
           :message "You do not have the authorization for this page"})
        (handler request)))))

(defn wrap-authorized [handler]
  (fn [{:keys [session] :as request}]
    (if (nil? (:identity session))
      {:status  302
       :headers {"Location" (str "/login?redirect=" (:uri request))}
       :body    ""}
      (handler request))))