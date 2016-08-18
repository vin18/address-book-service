(ns address-book.db-request-handler
  (:require [cheshire.core :refer :all]
            [address-book.address-book-store :refer [get-db-snapshot
                                                     validate-and-add-in-db
                                                     get-from-db
                                                     validate-and-edit-db
                                                     delete-from-db!
                                                     search-for-string
                                                     search-for-map]]))

(defn get-data-from-request-body
  "This method converts a string representation of map 
  into clojure-map also the keys are made into keywords"
  [body]
  (parse-string (slurp body) (fn [k] (keyword k))))

(defn get-all
  "Returns a map response for all entries in the database"
  []
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (generate-string (get-db-snapshot))})

(defn search-s
  "Returns a map-response for all the entries 
   which match the given string"
  [s]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (generate-string (search-for-string s))})

(defn search-m
  "Returns a map-response for all the entries
   which match the given pattern map"
  [body]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (generate-string
          (search-for-map (get-data-from-request-body body)))})

(defn add-new
  "Returns a map-response of adding a new entry in the database.
   Status is 201 if successful otherwise is 500 with appropriate failure message"
  [body]
  (try
    {:status 201
     :headers {"Content-Type" "application/json"}
     :body (generate-string
            {:id (validate-and-add-in-db (get-data-from-request-body body))})}
    (catch Exception e
      {:status 500
       :body (.getMessage e)})))

(defn get-by-id
  "Returns a map-response of trying to find the id in database.
   Status is 200 if successful, 404 otherwise"
  [id]
  (try
    (generate-string (get-from-db (keyword id)))
    (catch Exception e
      {:status 404
       :body (.getMessage e)})))

(defn edit-by-id
  "Returns a response of trying to edit an entry in database
   Returns 200 if successful, 500 otherwise with appropriate error message"
  [id body]
  (try
    (do
      (validate-and-edit-db (keyword id) (get-data-from-request-body body))
      "Record edited Successfully")
    (catch Exception e
      {:status 500
       :body (.getMessage e)})))

(defn delete-by-id
  "Returns a map- response of trying to delete an entry from the database
   identified by the 'id'. Status is 200 if successful, 500 otherwise"
  [id]
  (try
    (do
      (delete-from-db! (keyword id))
      "Record Deleted Successfully")
    (catch Exception e
      {:status 500
       :body (.getMessage e)})))
