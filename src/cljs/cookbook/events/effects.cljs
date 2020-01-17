(ns cookbook.events.effects
  (:require
    [re-frame.core :as rf]))

(defn- dispatch-or-fn [f resource-id]
  (cond
    (fn? f) #(do (rf/dispatch [:resources/loaded resource-id])
                 (f %))
    (sequential? f) #(do (rf/dispatch [:resources/loaded resource-id])
                         (rf/dispatch (conj f %)))))

(rf/reg-fx
  :http
  (fn [{:keys [method url resource-id params on-success on-error skip-delay? skip-loading-screen?]
        :or   {on-error [:common/ajax-error]}}]
    (if skip-loading-screen?
      (rf/dispatch [:resources/skip-loading-screen]))
    (rf/dispatch [:resources/load resource-id])
    (let [event #(method url {:handler       (dispatch-or-fn on-success resource-id)
                              :params        params
                              :error-handler (dispatch-or-fn on-error resource-id)})]
      (if skip-delay?
        (event)
        (js/setTimeout event 850)))))

(rf/reg-fx
  :side-effect
  (fn [f]
    (f)))