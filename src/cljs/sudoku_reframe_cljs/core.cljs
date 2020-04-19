(ns sudoku-reframe-cljs.core
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [sudoku-reframe-cljs.events :as events]
   [sudoku-reframe-cljs.views :as views]
   [sudoku-reframe-cljs.config :as config]
   ))


(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
