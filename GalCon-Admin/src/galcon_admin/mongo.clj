(ns galcon-admin.mongo
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [environ.core :refer [env]]
            [clj-time.core :as t]
            [monger.operators :refer :all]
            [clj-time.coerce :as c]
            [monger.conversion :refer [from-db-object]])
  (:import [com.mongodb MongoOptions ServerAddress]))

(def connection (mg/connect-via-uri (or (System/getenv "MONGOHQ_URL" "mongodb://localhost:27017/galcon")))

(defn find-user [handle]
(mc/find-one (:db connection) "users" {:handle handle}))

(defn find-active-users-since [time]
  (let [date (c/to-date (c/from-long time))]
  (mc/find-maps (:db connection) "users" { "session.expireDate" { $gte date}})))




