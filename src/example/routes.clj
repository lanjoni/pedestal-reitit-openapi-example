(ns example.routes
  (:require [clojure.java.io :as io] 
            [reitit.ring.spec]
            [reitit.coercion.malli]
            [reitit.swagger :as swagger]
            [reitit.ring.malli]))

(defn reitit-routes
  [_config]
  [["/swagger.json" {:get {:no-doc  true
                           :swagger {:info {:title       "my-api"
                                            :description "with [malli](https://github.com/metosin/malli) and reitit-ring"}
                                     :tags [{:name        "files",
                                             :description "file api"}
                                            {:name        "math",
                                             :description "math api"}]}
                           :handler (swagger/create-swagger-handler)}}]
   ["/files" {:swagger {:tags ["files"]}}
    ["/upload"
     {:post {:summary    "upload a file"
             :parameters {:multipart [:map [:file reitit.ring.malli/temp-file-part]]}
             :responses  {200 {:body [:map
                                      [:name string?]
                                      [:size int?]]}}
             :handler    (fn [{{{{:keys [filename
                                         size]} :file}
                                :multipart}
                               :parameters}]
                           {:status 200
                            :body   {:name filename
                                     :size size}})}}]
    ["/download" {:get {:summary "downloads a file"
                        :swagger {:produces ["image/png"]}
                        :handler (fn [_]
                                   {:status  200
                                    :headers {"Content-Type" "image/png"}
                                    :body    (-> "reitit.png"
                                                 (io/resource)
                                                 (io/input-stream))})}}]]
   ["/math" {:swagger {:tags ["math"]}}
    ["/plus"
     {:get  {:summary    "plus with malli query parameters"
             :parameters {:query [:map
                                  [:x
                                   {:title               "X parameter"
                                    :description         "Description for X parameter"
                                    :json-schema/default 42}
                                   int?]
                                  [:y int?]]}
             :responses  {200 {:body [:map [:total int?]]}}
             :handler    (fn [{{{:keys [x
                                        y]}
                                :query}
                               :parameters}]
                           {:status 200
                            :body   {:total (+ x y)}})}
      :post {:summary    "plus with malli body parameters"
             :parameters {:body [:map
                                 [:x
                                  {:title               "X parameter"
                                   :description         "Description for X parameter"
                                   :json-schema/default 42}
                                  int?]
                                 [:y int?]]}
             :responses  {200 {:body [:map [:total int?]]}}
             :handler    (fn [{{{:keys [x
                                        y]}
                                :body}
                               :parameters}]
                           {:status 200
                            :body   {:total (+ x y)}})}}]]])
