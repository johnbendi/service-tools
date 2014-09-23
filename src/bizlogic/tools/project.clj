(ns bizlogic.tools.project
  (:refer-clojure :exclude [read])
  (:require [leiningen.core.project :as project]
            [clojure.walk :as walk]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as s]
            [cemerick.pomegranate :as pomegranate]
            [cemerick.pomegranate.aether :as aether]
            [leiningen.core.utils :as utils]
            [leiningen.core.ssl :as ssl]
            [leiningen.core.user :as user]
            [leiningen.core.classpath :as classpath])
  (:import (clojure.lang DynamicClassLoader)
           (java.io PushbackReader)))

(defn read
  "Read project map out of file, which defaults to project.clj."
  ([file profiles]
     (locking read
       (binding [*ns* (find-ns 'bizlogic.tools.project)]
         (try (load-file file)
              (catch Exception e
                (throw (Exception. (format "Error loading %s" file) e)))))
       (let [project (resolve 'bizlogic.tools.project/project)]
         (when-not project
           (throw (Exception. (format "%s must define project map" file))))
         ;; return it to original state
         (ns-unmap 'bizlogic.tools.project 'project)
         (project/init-profiles
           (project/project-with-profiles @project) profiles))))
  ([file] (read file [:default]))
  ([] (read "project.clj")))
