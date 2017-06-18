(ns dev
  (:require [cemerick.piggieback :as pb]
            [fusion360.repl :as frepl]))


(defn fusion-repl
  "Start a Fusion-360 repl and wait for the app to connect"
  []
  (pb/cljs-repl (frepl/repl-env)))


