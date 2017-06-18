(ns fusion360.repl)

(defn send-message [sock msg-type value]
  (.send sock (.stringify js/JSON (clj->js {"type" msg-type
                                            "value" value}))))
(defn setup-socket-handlers [sock]
  (set! (.-onopen sock)  #(send-message sock "ready" "ready"))
  (set! (.-onclose sock) #(.. js/adsk -core -Application get -userInterface
                              (messageBox "Clojure repl connection lost")))
  (set! (.-onmessage sock) (fn [evt]
                             (try
                               (send-message sock "success" (-> evt .-data js/eval))
                               (catch js/EvalError ex
                                 (send-message sock "error" (.-message ex)))
                               (catch :default ex
                                 (send-message sock  "exception", (.-message ex)))))))

(defonce ^:dynamic *socket* nil)

(defn connect-to-repl
  ([]
   (connect-to-repl "http://127.0.0.1:9000/repl"))
  ([url]
   (when *socket* (.close *socket*))
   (let [s (new js/SockJS url)]
     (setup-socket-handlers s)
     (set! *socket* s))))

