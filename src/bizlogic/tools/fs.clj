(ns bizlogic.tools.fs
  "FileSystem Utils"
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [cemerick.pomegranate :as cp])
  (:import (java.io File)
           (java.nio.file Path)
           (java.nio.file Paths)))

;; Common operations
;; ls
(defn now [] (java.util.Date.))

(defn copy [])

(defn cd [])

(defn pwd [])

(defn ls [& fs]
  (doseq [f fs
          f (.listFiles (io/file (name f)))]
    (println (.getName f))))

(defn parent-dir [])

(defn relative-loc [])

;; {:a {:b 1 :c 2}} => [[:a :b 1] [:a :c 2]]
(defn all-paths [m]
  (for [[k v] m,
        :let [paths (if (map? v)
                      (all-paths v) [[v]])]
        path paths]
    (cons k path)))

(defn drop-leading-sep [s]
  (loop [s s]
    (if (.startsWith s File/separator)
      (recur (subs s 1))
      s)))

(defn drop-ending-sep [s]
  (loop [s s]
    (if (.endsWith s File/separator)
      (recur (subs s 0 (dec (.length s))))
      s)))

(defn path [& args]
  (reduce #(str (drop-ending-sep (clojure.string/trim %1)) "/"
                (drop-leading-sep (clojure.string/trim %2)))
          (first args) (rest args)))

(defn sanitize
  "Replace hyphens with underscores."
  [s]
  (string/replace s "-" "_"))

(defn load-config [project]
  (println project)
  (let [project' (sanitize (name project))
        apps-path "client/apps"]
    (cp/add-classpath (str (io/file apps-path project' "cljs")))
    (load-file (str (io/file apps-path  project' "config.clj")))))

#_
(defn load-config [project app]
  (let [apps-path "client/apps"
        project-cfg (load-file
                     (str apps-path "/"
                          (sanitize (name project))
                          "/config.clj"))]
    (assoc-in (project-cfg app) [:build :project-name] project)))

(defn- replace-strings-with-files [watched-files]
  (map (fn [{:keys [source] :as m}]
         (assoc m :source (if (string? source) (io/file source) source)))
       watched-files))

(defn clj-file?
  "Is the given file a ClojureScript file?"
  [f]
  (and (.isFile f)
       (.endsWith (.getName f) ".clj")))

(defn- jstr
  "Use the :js location provided in opts to construct a path to a
  JavaScript file."
  [public config & paths]
  (apply
   str public "/"
       (get-in config [:application :generated-javascript]) "/" paths))

(defn relative-path
  "Given a directory and a file, return the relative path to the file
  from within this directory."
  [dir file]
  (.substring (.getAbsolutePath (io/file file))
              (inc (.length (.getAbsolutePath (io/file dir))))))

(defn- split-path [s]
  (string/split s (re-pattern (java.util.regex.Pattern/quote File/separator))))

(defn ensure-directory [dir]
  (let [path (remove empty? (split-path (.toString dir)))]
    (loop [dir (io/file (first path))
           children (next path)]
      (when (not (.exists dir)) (.mkdir dir))
      (when children
        (recur (io/file dir (first children)) (next children))))
    dir))

(defn- ensure-ends-with-sep [p]
  (if (.endsWith p File/separator) p (str p File/separator)))

(def ^:dynamic *public* "client/out/public")
(def ^:dynamic *tools-public* "client/out/tools/public")

(defn- get-public [k]
  (when k
    (ensure-ends-with-sep
      (case k
        :public *public*
        :tools-public *tools-public*))))

(defn delete-files
  "Delete one or more files or directories. Directories are recursively
  deleted."
  [& paths]
  (doseq [path paths
          file (reverse (file-seq (io/file path)))]
    (.delete file)))
