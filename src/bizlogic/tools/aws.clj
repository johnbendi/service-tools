(ns bizlogic.tools.aws
  (:refer-clojure :exclude [name])
  (:require [amazonica.aws.ec2 :as ec2]
            [amazonica.aws.cloudformation :as cf]
            [amazonica.aws.dynamodb :as ddb]
            [amazonica.aws.sqs :as sqs]
            [amazonica.aws.glacier :as glacier]
            [amazonica.aws.autoscaling :as as]
            [clojure.core.async :as async :refer [go >! <! alts! timeout]]
            [clojure.java.io :as io]
            [bizlogic.tools.util :refer [name]]
            ))

(def ^:dynamic *base-dir* "/home/chijioke/projects/clojure/servers")

(defn deploy [project {:keys [dir]}]
  (binding [*base-dir* dir]
    (let [project-dir (str (io/file *base-dir* (name project)))])))
