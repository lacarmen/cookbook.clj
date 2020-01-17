(ns cookbook.core
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   [reitit.core :as reitit]
   [reitit.frontend.easy :as rfe]
   [cookbook.ajax :as ajax]
   [cookbook.events.effects]
   [cookbook.events.handlers]
   [cookbook.events.subscriptions]
   [cookbook.pages.home :refer [home-page]]
   [cookbook.routes :as routes]))

;; -------------------------
;; Initialize app
(defn ^:dev/after-load mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'routes/page] (.getElementById js/document "app")))

;; -------------------------
;; Routes
(def base-title "cookbook")

(defn set-page-title! [match]
  (set! (.-title js/document)
        (str base-title
             (when-let [page (get-in match [:data :title])]
               (str " | " page)))))

(defn navigate! [match _]
  (let [admin?    true #_(-> js/identity clj->js :admin)
        req-admin? (get-in match [:data :admin?])]
    (set-page-title! match)
    (if req-admin?
      (if admin?
        (rf/dispatch [:common/navigate match])
        (rf/dispatch [:common/redirect! "/login"]))
      (rf/dispatch [:common/navigate match]))))

(defn start-router! []
  (rfe/start!
    (reitit/router routes/routes)
    navigate!
    {:use-fragment false}))

(defn init! []
  (start-router!)
  (ajax/load-interceptors!)
  (mount-components))
