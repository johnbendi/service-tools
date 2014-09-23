(ns bizlogic.tools.io
  "FileSystem Utils"
  (:require [clojure.java.io :as io]))

;; Common operations
;; ls

(defn cd [])

(defn pwd [])

(defn ls [& fs]
  (doseq [f fs
          f (.listFiles (io/file (name f)))]
    (println (.getName f))))

(defn parent-dir [])

(defn relative-loc [])

(defn rename [old new]
  (.renameTo old new))
