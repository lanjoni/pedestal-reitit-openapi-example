(ns example.core
  (:require [io.pedestal.http.route]
            [reitit.interceptor]
            [io.pedestal.http :as http]
            [reitit.ring.malli]
            [reitit.http]
            [reitit.pedestal :as reitit-pedestal]
            [muuntaja.core]
            [example.server :as server]))

(defonce server (atom nil))

(defonce all-started-servers (atom []))

(defn start-server! [port]
  (locking server
    (if @server
      ::server-already-running
      (let [config (server/config port)
            new-server (-> config
                           http/default-interceptors
                           (reitit-pedestal/replace-last-interceptor (server/reitit-http-router config))
                           http/dev-interceptors
                           http/create-server
                           http/start)]
        (reset! server new-server)
        (swap! all-started-servers conj new-server)
        ::server-started))))

(defn stop-server! []
  (locking server
    (if @server
      (do
        (http/stop @server)
        (reset! server nil)
        ::server-stopped)
      ::server-already-stopped)))

(defn -main
  "Start webserver"
  [& args]
  (start-server! 3000)
  (println "Server started running at port 3000"))

(comment
  ;; if you're running a REPL,
  ;; just evaluate this to start
  (start-server! 3000)
  ;; and this to stop
  (stop-server!))
