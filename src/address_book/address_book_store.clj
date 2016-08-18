(ns address-book.address-book-store
  (:require [address-book.db-schema :refer [validate-data]]
            [cheshire.core :refer :all]
            [address-book.map-utils :refer [is-pattern-match]]))

;; -------------------------------------------
;; Implement the db as an atom of map of maps
;; -------------------------------------------
(defn ^:private db-validator
  "A validator function for atom which verifies that the atom 
   will not accept swap! with value that duplicates particular key values
   namely :name and :email. So no two entries in the atom can have the same 
  :name or :email"
  [new-db-value]
  (let [has-duplicates? (fn [xs]
                          ((comp (partial < 0) count)
                           (filter
                            (partial (comp (partial < 1) count))
                            (partition-by identity xs))))]
    (cond
      (has-duplicates? (map :name (vals new-db-value))) (throw
                                                         (IllegalStateException. "Duplicate Name"))
      (has-duplicates? (map :email (vals new-db-value))) (throw
                                                          (IllegalStateException. "Duplicate Email"))
      :else true)))

(def ^:private in-memory-db (atom {} :validator db-validator))

(defn reset-db
  "Only used for testing"
  []
  (reset! in-memory-db {}))

(defn get-db-snapshot
  "This returns the dereferenced value of the atom (DB)"
  []
  @in-memory-db)

(defn add-in-db!
  "This method swaps the in-memory-db atom by adding a new key value pair
  The key is auto-generated and also returned iff swap is successful"
  [data]
  (let [unique_id (str (gensym))]
    (do
      (swap! in-memory-db conj [(keyword unique_id) data])
      unique_id)))

(defn get-from-db
  "This returns the value in the atom identified by the arg key"
  [id]
  (if-let [data (id (get-db-snapshot))]
    data
    (throw (Exception. "Not Found"))))

(defn validate-and-add-in-db
  "swap atom iff the data has a valid schema"
  [data]
  ((comp add-in-db! validate-data) data))

(defn edit-data-in-db!
  "Try and swap atom iff the key 'id' is present 
  otherwise throw Not found excepion"
  [id new-data]
  (swap! in-memory-db
         #(conj %
                (if (contains? % id)
                  [id new-data]
                  (throw (Exception. "ID Not Found"))))))

(defn delete-from-db!
  "Delete from db (swap atom) iff the id is present in the atom"
  [id]
  (swap!
   in-memory-db
   #(dissoc %
            (if (contains? % id)
              id
              (throw (Exception. "ID Not Found"))))))

(defn validate-and-edit-db
  "Validate the schema before calling edit-data-in-db!"
  [id new-data]
  (edit-data-in-db! id (validate-data new-data)))

(defn search-for-string
  "Search the current value of atom, for values which match the given string"
  [sstr]
  (filter
   (fn [xs]
     (>
      (count (remove nil? (map
                           (partial re-find (re-pattern (str "(?i)" sstr)))
                           (vals xs))))
      0))
   (vals (get-db-snapshot))))

(defn search-for-map
  "Search the current value of atom using a map as a pattern"
  [find-map]
  (filter
   (partial is-pattern-match find-map)
   (vals (get-db-snapshot))))
