(ns bizlogic.tools.session
  (:require [datomic.api :as d]
            [ring.middleware.session.store :as store]))

(d/create-database "datomic:mem://session")

(def session-conn (d/connect "datomic:mem://session"))

(def session-schema  [{:db/id (d/tempid :db.part/id)
                       :db/ident :session/key
                       :db/valueType :db.type/uuid
                       :db/cardinality :db.cardinality/one
                       :db/doc "Session Key"
                       :db.install/_attribute :db.part/db}
                      {:db/id (d/tempid :db.part/db)
                       :db/ident :user/username
                       :db/valueType :db.type/string
                       :db/cardinality :db.cardinality/one
                       :db/doc "User's username"
                       :db.install/_attribute :db.part/db}])

(d/transact session-conn session-schema)

(defn session-store [conn]
  (reify
    store/SessionStore
    (read-session [store key]
      (d/q '[:find ?data
             :where
             [?e :session/key key]
             [?e :session/data ?data]]
        (d/db conn)))
    (write-session [store key data]
      (d/transact conn [{:session/key key
                         :db/id (d/tempid :db.part/db)
                         :session/data data}]))
    (delete-session [store key]
      (d/transact conn [[:db/retract :session/key key]]))))
