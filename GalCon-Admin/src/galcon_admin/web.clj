(ns galcon-admin.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.adapter.jetty :as jetty]
            [galcon-admin.mongo :as mongo]
            [environ.core :refer [env]]))

(defn splash []
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body (pr-str [(mongo/find-user "AI")])})

(defn find-user-by-handle [handle]
  {:status 200
  :headers {"Content-Type" "application/json"}
  :body (pr-str (mongo/find-user handle))})

(defn find-users-active-since [sessionDate]
  {:status 200
  :headers {"Content-Type" "application/json"}
  :body (pr-str (mongo/find-active-users-since (read-string sessionDate)))})

(defroutes app
  (GET "/" []
       (splash))
  (GET "/user/:handle" {{handle :handle} :params}
       (find-user-by-handle handle))
  (GET "/users" {{sessionDate :sessionDate} :params}
       (find-users-active-since sessionDate))
  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (site #'app) {:port port :join? false})))

;; For interactive development:
;;(def server (-main))
;; (.stop server)
 