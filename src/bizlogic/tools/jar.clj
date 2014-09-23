(ns bizlogic.tools.jar
  )

(defmulti ^:private copy-to-jar (fn [project jar-os acc spec] (:type spec)))

(defn- relativize-path
  "Relativizes a path: Removes the root-path of a path if not already removed."
  [path root-path]
  (if (.startsWith path root-path)
    (.substring path (.length root-path))
    path))

(defn- full-path ;; Q: is this a good name for this action?
  "Appends the path string with a '/' if the file is a directory."
  [file path]
  (if (.isDirectory file)
    (str path "/")
    path))

(defn- dir-string
  "Returns the file's directory as a string, or the string representation of the
  file itself if it is a directory."
  [file]
  (if-not (.isDirectory file)
    (str (.getParent file) "/")
    (str file "/")))

(defn- put-jar-entry!
  "Adds a jar entry to the Jar output stream."
  [jar-os file path]
  (.putNextEntry jar-os (doto (JarEntry. path)
                          (.setTime (.lastModified file))))
  (when-not (.isDirectory file)
    (io/copy file jar-os)))

(defmethod copy-to-jar :path [project jar-os acc spec]
  (let [root-file (io/file (:path spec))
        root-dir-path (unix-path (dir-string root-file))
        paths (for [child (file-seq root-file)
                    :let [path (relativize-path
                                 (full-path child (unix-path (str child)))
                                 root-dir-path)]]
                (when-not (or (skip-file? child path root-file
                                (:jar-exclusions project))
                            (added-file? child path acc))
                  (put-jar-entry! jar-os child path)
                  path))]
    (into acc paths)))

(defmethod copy-to-jar :paths [project jar-os acc spec]
  (reduce (partial copy-to-jar project jar-os) acc
    (for [path (:paths spec)]
      {:type :path :path path})))

(defmethod copy-to-jar :bytes [project jar-os acc spec]
  (let [path (unix-path (:path spec))]
    (when-not (some #(re-find % path) (:jar-exclusions project))
      (.putNextEntry jar-os (JarEntry. path))
      (let [bytes (if (string? (:bytes spec))
                    (.getBytes (:bytes spec))
                    (:bytes spec))]
        (io/copy (ByteArrayInputStream. bytes) jar-os)))
    (conj acc path)))

(defmethod copy-to-jar :fn [project jar-os acc spec]
  (let [f (eval (:fn spec))
        dynamic-spec (f project)]
    (copy-to-jar project jar-os acc dynamic-spec)))

;; possibly make maniffest from project.clj or some other config
(defn jar [dir target & manifest]
  (let [zipinput (ZipInputStream. dir)
        file-output (io/file target)
        jar-output (-> file-output
                     (FileOutputStream.)
                     (BufferedOutputStream.)
                     (JarOutputStream. manifest))
        zip-entries (cond
                      ( ))]))
