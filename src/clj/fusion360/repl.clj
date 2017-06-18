(ns fusion360.repl
  (:require [compojure.route :as route]
            [sockjs.session :as session]
            [cljs.repl :as repl]
            [compojure.core :refer [routes GET]]
            [compojure.handler :as handler ]
            [ring.middleware.params :as rmp]
            [ring.middleware.reload :as rmr]
            [org.httpkit.server :as http-kit]
            [sockjs.core :refer [sockjs-handler]]
            [cheshire.core :as cheshire]
            [clojure.core.async :as async])
  ;;(:gen-class)
  )

(defn all-routes [env]
  (routes
   (GET "/" [] "fusion repl server, try /repl")
   (sockjs-handler "/repl" env {:response-limit 65536})
   (route/not-found "<p>Page not found.</p>")))

(defn start-server [env]
  (println "starting server on port " (:port env))
  (http-kit/run-server
   (-> (all-routes env)
       rmp/wrap-params
       rmr/wrap-reload
       handler/site)
   {:port (:port env)})
  (println "waiting for client to connect.."))

(defmulti handle-message (fn [env msg] (:type msg)))

(defmethod handle-message "ready" [env msg]
  (println "Client connected")
  {:status :success :value (:value msg)})

(defmethod handle-message "success" [env msg]
  {:status :success :value (:value msg)})

(defmethod handle-message "exception" [env msg]
  ;; get stack trace?
  {:status :exception :value (:value msg)})

(defmethod handle-message "default" [env msg] ;; :error
  {:status :error :value (:value msg)})


(defn fusion-eval [env js]
  (session/send! @(:socket env) {:type :msg :content js})
  (async/<!! (:resp-chan env)))

(defn fusion-load-file [env url]
  (fusion-eval env (slurp url)))

(defn shutdown [{:keys [socket stop-fn]}]
  (when @stop-fn
    (@stop-fn)
    (reset! stop-fn nil))
  
  #_(when @socket
    (session/close! @socket 3000 "server shutting down")
    (reset! socket nil)))


(defrecord SocksJSEnv [port socket stop-fn resp-chan]
  session/SockjsConnection
  (session/on-open [this s]
    (reset! socket s))
  (session/on-message [this s msg]
    (let [msg (cheshire/decode msg keyword)]
      (async/>!! resp-chan (handle-message this msg))))
  (session/on-close [this s]
    (println "client disconnected")
    (reset! socket nil)
    nil)
  repl/IJavaScriptEnv
  (-setup [repl-env opts]
    (reset! stop-fn (start-server repl-env))
    (async/<!! resp-chan)
    nil)
  (-evaluate [repl-env filename line js]
    (fusion-eval repl-env js))
  (-load [repl-env provides url]
    (fusion-load-file repl-env url))
  (-tear-down [repl-env]
    (shutdown repl-env)
    nil))

(defn repl-env*  [{:keys [port] :as opts} ]
  (merge (->SocksJSEnv 9000 (atom nil) (atom identity) (async/chan 1))
         opts))

(defn repl-env
  "Returns a fresh JS environment, suitable for passing to repl.
  Hang on to return for use across repl calls."
  [& {:as opts}]
  (repl-env* opts))

(defn -main []
  (repl/repl (repl-env)))

;; use this for development

(comment

  (require '[cemerick.piggieback :as pb])
  (pb/cljs-repl (repl-env))
  (def ui (.. js/adsk -core -Application get -userInterface))
  (.messageBox ui "Hello...")
  
  (do
    (def env (repl-env))
    (repl/-setup env {})
    (fusion-eval @(:socket env) "adsk.core.Application.get().userName")
    (repl/repl env))

  (let [start (.getTime (js/Date.))]
    (.. js/adsk -core -Application get -userName)
    (- (.getTime (js/Date.)) start))
  )
