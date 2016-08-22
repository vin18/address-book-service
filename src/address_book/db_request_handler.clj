(ns address-book.db-request-handler
  (:require [cheshire.core :refer :all]
            [address-book.address-book-store :refer [get-db-snapshot
                                                     validate-and-add-in-db
                                                     get-from-db
                                                     validate-and-edit-db
                                                     delete-from-db!
                                                     search-for-string
                                                     search-for-map]]))

(defn bind-error
  [f [result err]]
  (if (nil? err)
    (f result)
    [nil err]))

(defn bind-error-generate-string
  [[result err]]
  (if (nil? err)
    [(generate-string result) nil]
    [nil err]))

(defn bind-response
  [result err ok-status err-status]
  (if (nil? err)
    {:status ok-status
     :headers {"Content-Type" "application/json"}
     :body result}
    {:status err-status
     :body err}))

(defn get-data-from-request-body
  "This method converts a string representation of map 
  into clojure-map also the keys are made into keywords"
  [body]
  (try
    [(parse-string (slurp body) (fn [k] (keyword k))) nil]
    (catch Exception e [nil (.getMessage e)])))

(defn get-all
  "Returns a map response for all entries in the database"
  []
  (apply bind-response
         (concat
          (bind-error-generate-string (get-db-snapshot))
          [200 400])))

(defn search-s
  "Returns a map-response for all the entries 
   which match the given string"
  [s]
  (apply bind-response
         (concat
          (bind-error-generate-string (search-for-string s))
          [200 400])))

(defn search-m
  "Returns a map-response for all the entries
   which match the given pattern map"
  [body]
  (apply bind-response
         (concat
          (bind-error-generate-string
           (bind-error search-for-map (get-data-from-request-body body)))
          [200 400])))

(defn add-new
  "Returns a map-response of adding a new entry in the database.
   Status is 201 if successful otherwise is 500 with appropriate failure message"
  [body]
  (apply bind-response
         (concat
          (bind-error-generate-string
           (bind-error validate-and-add-in-db (get-data-from-request-body body)))
          [201 400])))

(defn get-by-id
  "Returns a map-response of trying to find the id in database.
   Status is 200 if successful, 404 otherwise"
  [id]
  (apply bind-response
         (concat
          (bind-error-generate-string (get-from-db (keyword id)))
          [200 400])))

(defn edit-by-id
  "Returns a response of trying to edit an entry in database
   Returns 200 if successful, 500 otherwise with appropriate error message"
  [id body]
  (apply bind-response
         (concat
          (bind-error
           (partial validate-and-edit-db (keyword id))
           (get-data-from-request-body body))
          [200 400])))

(defn delete-by-id
  "Returns a map- response of trying to delete an entry from the database
   identified by the 'id'. Status is 200 if successful, 500 otherwise"
  [id]
  (apply bind-response
         (concat
          (delete-from-db! (keyword id))
          [200 400])))
