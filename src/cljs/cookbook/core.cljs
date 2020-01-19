(ns cookbook.core
  (:require
    [re-frame.core :as rf]
    [reagent.core :as r]
    [reitit.core :as reitit]
    [reitit.frontend.easy :as rfe]
    ;;
    [cookbook.ajax :as ajax]
    [cookbook.common.effects]
    [cookbook.common.events]
    [cookbook.common.subscriptions]
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
  (let [admin?     (-> js/identity clj->js :admin)
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

(defn load-page-data []
  (when-let [user (js->clj js/identity :keywordize-keys true)]
    (rf/dispatch-sync [:common/init-db user])))

(defn init! []
  (start-router!)
  (load-page-data)
  (ajax/load-interceptors!)
  (mount-components))
