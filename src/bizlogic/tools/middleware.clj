(ns bizlogic.tools.middleware
  (:require [bizlogic.tools.log :as log]
            [io.pedestal.interceptor :refer [defon-response]]
            [ring.util.mime-type :as mime]
            [ring.middleware.resource :as resource]
            [io.pedestal.interceptor :as interceptor
             :refer [interceptor definterceptorfn defon-request
                     defon-response defmiddleware]]
            [ring.middleware.edn :as edn]
            [io.pedestal.http.route :as route
             ;;:refer [print-routes]
             ]
            [clauth.token :as token]
            [clauth.middleware :as clmw]
            [ring.util.response :as ring-resp])
  (:import java.io.File))

(defon-response js-encoding
  [{:keys [headers body] :as response}]
  (if (and (= (get headers "Content-Type") "text/javascript")
           (= (type body) File))
    (assoc-in response [:headers "Content-Type"]
              "text/javascript; charset=utf-8")
    response))

(defn response-fn-adapter
  "Adapts a ring middleware fn taking a response and request to an interceptor context."
  [response-fn & [opts]]
  (fn [{:keys [request response] :as context}]
    (if response
      (assoc context :response (if opts
                                 (response-fn response request opts)
                                 (response-fn response request)))
      context)))

(defn- leave-interceptor
  "Defines an leave only interceptor given a ring fn."
  [name response-fn & args]
  (interceptor/after name (apply response-fn-adapter response-fn args)))

(defn- content-type-response
  "Tries adding a content-type header to response by request URI (unless one
  already exists)."
  [resp req & [opts]]
  (if-let [mime-type (or (get-in resp [:headers "Content-Type"])
                         (mime/ext-mime-type (:uri req) (:mime-types opts)))]
    (assoc-in resp [:headers "Content-Type"] mime-type)
    resp))

(definterceptorfn content-type
  "Interceptor for content-type ring middleware."
  [& [opts]]
  (leave-interceptor ::content-type-interceptor content-type-response opts))

(definterceptorfn resource
  "Interceptor for resource ring middleware."
  [root-path]
  (interceptor/handler
   ::resource
   (fn [request]
     (if-let [response (resource/resource-request request root-path)]
       response
       request))))

(defn- edn-request?
  [req]
  (if-let [^String type (:content-type req)]
    (not (empty? (re-find #"^application/(vnd.+)?edn" type)))))

(defon-request edn-params
  [request]
  (if-let [body (and (edn-request? request) (:body request))]
    (let [edn-params (binding [*read-eval* false] (edn/-read-edn body))]
      (assoc request
        :edn-params edn-params
        :params (merge (:params request) edn-params)))
    request))

(interceptor/defon-request print-routes
  "Log the request's method and uri."
  [var request]
  (route/print-routes (deref var))
  request)

;; oauth and session ================================================

(definterceptorfn require-bearer-token [handler]
  (interceptor/on-request
    (fn [request]
      )))

(definterceptorfn verify-bearer-token [& [find-token-fn]]
  (interceptor/on-request
    (fn [req]
      (if-let [access-token (clmw/req->token req find-token-fn)]
        (assoc req :access-token access-token)
        req))))

ring-resp/redirect
