(ns example.server
  (:require [io.pedestal.http.route]
            [reitit.interceptor]
            [reitit.dev.pretty :as pretty]
            [reitit.coercion.malli :as malli]
            [reitit.ring :as reitit-ring]
            [reitit.ring.malli]
            [reitit.http]
            [reitit.pedestal :as reitit-pedestal]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [reitit.http.coercion :as coercion]
            [reitit.http.interceptors.parameters :as parameters]
            [reitit.http.interceptors.muuntaja :as muuntaja]
            [reitit.http.interceptors.multipart :as multipart]
            [muuntaja.core]
            [malli.util :as mu]
            [example.routes :as routes]))

(defn reitit-ring-routes
  [_config]
  [(swagger-ui/create-swagger-ui-handler
    {:path   "/"
     :config {:validatorUrl     nil
              :operationsSorter "alpha"}})
   (reitit-ring/create-resource-handler)
   (reitit-ring/create-default-handler)])

(defn reitit-router-config
  [_config]
  {:exception pretty/exception
   :data      {:coercion     (malli/create
                              {:error-keys       #{:coercion
                                                   :in
                                                   :schema
                                                   :value
                                                   :errors
                                                   :humanized}
                               :compile          mu/closed-schema
                               :strip-extra-keys true
                               :default-values   true
                               :options          nil})
               :muuntaja     muuntaja.core/instance
               :interceptors [swagger/swagger-feature
                              (parameters/parameters-interceptor)
                              (muuntaja/format-negotiate-interceptor)
                              (muuntaja/format-response-interceptor)
                              (muuntaja/format-request-interceptor)
                              (coercion/coerce-response-interceptor)
                              (coercion/coerce-request-interceptor)
                              (multipart/multipart-interceptor)]}})

(defn config 
  [port]
  {:env                             :dev
   :io.pedestal.http/routes         []
   :io.pedestal.http/type           :jetty
   :io.pedestal.http/port           port
   :io.pedestal.http/join?          false
   :io.pedestal.http/secure-headers {:content-security-policy-settings
                                     {:default-src "'self'"
                                      :style-src   "'self' 'unsafe-inline'"
                                      :script-src  "'self' 'unsafe-inline'"}}
   ::reitit-routes routes/reitit-routes
   ::reitit-ring-routes reitit-ring-routes
   ::reitit-router-config reitit-router-config})

(defn reitit-http-router
  [{::keys [reitit-routes
            reitit-ring-routes
            reitit-router-config]
    :as    config}]
  (reitit-pedestal/routing-interceptor
   (reitit.http/router
    (reitit-routes config)
    (reitit-router-config config))
   (->> config
        reitit-ring-routes
        (apply reitit-ring/routes))))
