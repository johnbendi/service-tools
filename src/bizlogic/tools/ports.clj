(ns bizlogic.tools.port
  (:require [datomic.api :as d]))

(def ports-uri "datomic:://localhost:4334/ports")

(def conn (d/connect ports-uri))
