(ns cookbook.db.core
  (:require
    [cheshire.core :refer [generate-string parse-string parse-string-strict]]
    [clojure.java.jdbc :as jdbc]
    [clojure.string :as string]
    [clojure.tools.logging :as log]
    [clojure.walk :refer [postwalk]]
    [conman.core :as conman]
    [java-time :as jt]
    [java-time.pre-java8]
    [mount.core :refer [defstate]]
    ;;
    [cookbook.config :refer [env]])
  (:import org.postgresql.util.PGobject
           java.sql.Array
           clojure.lang.IPersistentMap
           clojure.lang.IPersistentVector))

(defstate ^:dynamic *db*
  :start (if-let [jdbc-url (:database-url env)]
           (conman/connect! {:jdbc-url jdbc-url})
           (do
             (log/warn "database connection URL was not found, please set :database-url in your config, e.g: dev-config.edn")
             *db*))
  :stop (conman/disconnect! *db*))

(conman/bind-connection *db* "sql/queries.sql")

(defn ->kebab-case-keyword* [k]
  (-> (reduce
        (fn [s c]
          (if (and
                (not-empty s)
                (Character/isLowerCase (last s))
                (Character/isUpperCase c))
            (str s "-" c)
            (str s c)))
        "" (name k))
      (string/replace #"[\s]+" "-")
      (.replaceAll "_" "-")
      (.toLowerCase)
      (keyword)))

(def ->kebab-case-keyword (memoize ->kebab-case-keyword*))

(defn transform-keys [t coll]
  "Recursively transforms all map keys in coll with t."
  (letfn [(transform [[k v]] [(t k) v])]
    (postwalk (fn [x] (if (map? x) (into {} (map transform x)) x)) coll)))

(defn unified-map-returning [x]
  (if (map? x) x (first x)))

(defn unified-handling-single [this result options]
  (case (:command options)
    :i!                (unified-map-returning result)
    :insert            (unified-map-returning result)
    :<!                (hugsql.adapter/result-one this result options)
    :returning-execute (hugsql.adapter/result-one this result options)
    :!                 (hugsql.adapter/result-one this result options)
    :execute           (hugsql.adapter/result-one this result options)
    :?                 (hugsql.adapter/result-one this result options)
    :query             (hugsql.adapter/result-one this result options)))

(defn result-one-snake->kebab
  [this result options]
  (->> (unified-handling-single this result options)
       (transform-keys ->kebab-case-keyword*)))

(defn result-many-snake->kebab
  [this result options]
  (->> (hugsql.adapter/result-many this result options)
       (map #(transform-keys ->kebab-case-keyword %))))

(defmethod hugsql.core/hugsql-result-fn :1 [sym]
  'cookbook.db.core/result-one-snake->kebab)

(defmethod hugsql.core/hugsql-result-fn :* [sym]
  'cookbook.db.core/result-many-snake->kebab)

(extend-protocol jdbc/IResultSetReadColumn
  java.sql.Timestamp
  (result-set-read-column [v _2 _3]
    (.toLocalDateTime v))
  java.sql.Date
  (result-set-read-column [v _2 _3]
    (.toLocalDate v))
  java.sql.Time
  (result-set-read-column [v _2 _3]
    (.toLocalTime v))
  Array
  (result-set-read-column [v _ _] (vec (.getArray v)))
  PGobject
  (result-set-read-column [pgobj _metadata _index]
    (let [type  (.getType pgobj)
          value (.getValue pgobj)]
      (case type
        "json" (parse-string-strict value true)
        "jsonb" (parse-string-strict value true)
        "citext" (str value)
        value))))

(defn to-pg-json [value]
  (doto (PGobject.)
    (.setType "jsonb")
    (.setValue (generate-string value))))

(extend-type clojure.lang.IPersistentVector
  jdbc/ISQLParameter
  (set-parameter [v ^java.sql.PreparedStatement stmt ^long idx]
    (let [conn      (.getConnection stmt)
          meta      (.getParameterMetaData stmt)
          type-name (.getParameterTypeName meta idx)]
      (if-let [elem-type (when (= (first type-name) \_) (apply str (rest type-name)))]
        (.setObject stmt idx (.createArrayOf conn elem-type (to-array v)))
        (.setObject stmt idx (to-pg-json v))))))

(extend-protocol jdbc/ISQLValue
  java.util.Date
  (sql-value [v]
    (java.sql.Timestamp. (.getTime v)))
  java.time.LocalTime
  (sql-value [v]
    (jt/sql-time v))
  java.time.LocalDate
  (sql-value [v]
    (jt/sql-date v))
  java.time.LocalDateTime
  (sql-value [v]
    (jt/sql-timestamp v))
  java.time.ZonedDateTime
  (sql-value [v]
    (jt/sql-timestamp v))
  IPersistentMap
  (sql-value [value] (to-pg-json value))
  IPersistentVector
  (sql-value [value] (to-pg-json value)))

(comment

  (get-user-by-id {:id "carmen"})
  (get-recipe-by-id {:id 3})
  (get-recipe-headers)

  (do
    (create-recipe! {:tags   ["instant pot"]
                     :recipe {:title       "Beef curry"
                              :ingredients ["1 can coconut milk" "beef"]
                              :directions  "1. turn on instant pot\n1. add beef and coconut milk\n1. cook"}
                     :author "carmen"})

    (create-recipe! {:tags   ["instant pot" "curry"]
                     :recipe {:title       "Green curry"
                              :ingredients ["1 can coconut milk" "chicken"]
                              :directions  "1. turn on instant pot\n1. add chicken and coconut milk\n1. cook"}
                     :author "carmen"})
    (create-recipe! {:tags   ["thai"]
                     :recipe {:title       "Fried rice"
                              :description "tasty fried rice"
                              :ingredients ["1 1/2 cups cooked rice" "1 cup shrimp" "1 tbsp fish sauce" "veggies"]
                              :directions  "1. cook **shrimp**\n1. add **veggies** and cook\n1. add **rice** and cook"}
                     :author "carmen"})
    (create-recipe! {:id 4,
                     :tags ["thai" "drink"],
                     :recipe {:title "Thai Iced Tea",
                              :directions "1. brew tea\n2. add sugar, and milk\n3. chill\n4. serve with ice",
                              :description "Refreshing Thai iced tea for a hot summer day",
                              :ingredients ["tea" "sugar" "condensed milk" "evaporated milk"]},
                     :author "carmen"}))

  )
