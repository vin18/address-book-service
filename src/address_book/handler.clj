(ns address-book.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [cheshire.core :refer :all]
            [ring.middleware.defaults :refer [wrap-defaults
                                              api-defaults]]
            [address-book.db-request-handler :refer [get-all
                                                     add-new
                                                     get-by-id
                                                     edit-by-id
                                                     delete-by-id
                                                     search-s
                                                     search-m]]))

(defroutes app-routes
  (POST "/address-book/" {body :body} (add-new body))
  (GET "/address-book/:id{G__[0-9]+}/" [id] (get-by-id id))
  (PUT "/address-book/:id{G__[0-9]+}/" {body :body {id :id} :params} (edit-by-id id body))
  (DELETE "/address-book/:id{G__[0-9]+}/" [id] (delete-by-id id))
  (GET "/address-book/search/:sstr{[a-zA-Z0-9]+}" [sstr] (search-s sstr))
  (POST "/address-book/search/" {body :body} (search-m body))
  (GET "/address-book/" [] (get-all))
  (route/not-found "Not Found"))

(def app
  "Defines the app with wrap-defaults and api-defaults"
  (wrap-defaults app-routes api-defaults))
