(ns cookbook.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[cookbook started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[cookbook has shut down successfully]=-"))
   :middleware identity})
