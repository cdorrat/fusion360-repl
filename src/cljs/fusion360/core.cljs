(ns fusion360.core
  (:require [fusion360.repl :as repl]))

(defn fusion-app []
  (.. js/adsk -core -Application get))


(defn create-ui []
  (let [ui (.-userInterface (fusion-app))
        qatToolbar (.. ui -toolbars (itemById "QAT"))
        cmdDefinitions  (.-commandDefinitions ui)
        on-command-created (fn [args]
                             (repl/connect-to-repl)
                             (.messageBox ui "Connecting to repl at http://127.0.0.1:9000/repl"))
        cmdDef (or (.itemById cmdDefinitions "ClojureReplDefId")
                   (doto (.addButtonDefinition cmdDefinitions
                                               "ClojureReplDefId" "Clojure Repl"
                                               "Clojure repl support" "./res/clojure")
                     (.. -commandCreated (add on-command-created))))
        newControl (.. qatToolbar -controls (addCommand cmdDef))]

    (set! (.-isPromotedByDefault newControl) true)
    (set! (.-isPromoted newControl) true)))

(defn ^:export run [context]
  (create-ui))
