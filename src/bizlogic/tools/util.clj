(ns bizlogic.tools.util
  (:require [clojure.string :as str]
            [clojure.string :as string]
            [io.pedestal.interceptor :as interceptor]
            [io.pedestal.impl.interceptor :as int-impl]
            [bizlogic.tools.log :as log]
            [ring.util.response :as ring-response]
            [clojure.java.io :as io])
  (:import (java.io File FileOutputStream BufferedOutputStream
             ByteArrayOutputStream)
           (java.util.zip ZipInputStream)
           (java.util.jar JarFile Manifest JarEntry JarOutputStream)
           (java.util.regex Pattern)))

(defn insert-at [n x coll]
  (vec (concat (take n coll) [x] (drop (inc n) coll))))

(defn- ensure-ends-with-sep [p]
  (if (.endsWith p File/separator) p (str p File/separator)))

(defn trim-left-right [coll]
  (map (fn [x]
         (-> (#(str/replace-first (str/reverse x) "-" ""))
             (#(str/replace-first (str/reverse %) "-" ""))))
       coll))

(def email-re #"\b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}\b")

(defn name [x]
  (if x (clojure.core/name x) ""))

(interceptor/defon-request log-request
  "Log the request's method and uri."
  [request]
  (log/info
    :msg (format "%s %s"
                         (string/upper-case (name (:request-method request)))
                         (:uri request))
    :headers (:headers request))
  request)

(interceptor/defon-response log-response
  "Log the request's method and uri."
  [response]
  (log/info :msg (format "Final response map: %s"
                         response))
  response)

(defn- response?
  "A valid response is any map that includes an integer :status
  value."
  [resp]
  (and (map? resp)
       (integer? (:status resp))))

(interceptor/defafter not-found
  "An interceptor that returns a 404 when routing failed to resolve a route."
  [context]
  (if-not (response? (:response context))
    (assoc context :response (ring-response/not-found (pr-str "Not Found!!")))
    context))

(interceptor/defbefore print-request
  [{:keys [request] :as ctx}]
  (let [intcpt (:name (peek (pop (::int-impl/stack ctx))))]
    (println (format "Printing request after Interceptor : %s " intcpt))
    (println request)
    ctx))

;; possibly make maniffest from project.clj or some other config
(defn jar [dir target & manifest]
  (let [zipinput (ZipInputStream. dir)
        file-output (io/file target)
        jar-output (-> file-output
                     (FileOutputStream.)
                     (BufferedOutputStream.)
                     (JarOutputStream. manifest))
        zip-entries (cond
                      )]))

(def tomcat-webapps "/opt/tomcat/tomcat7/base/webapps")

(defn deploy-tomcat [jarname])
