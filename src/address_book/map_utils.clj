(ns address-book.map-utils
  (:require [clojure.set :refer [intersection]]))

(defn un-nest-map
  "A flatten-like function for map. This will un-nest nested maps
   e.g. {:a 1 :b {:c 2 :d 3}} will become {:a 1 :c 2 :d 3}"
  [m]
  (reduce-kv
   (fn [intermediate-map k v]
     (conj
      intermediate-map
      (if (map? v) (un-nest-map v) {k v})))
   {}
   m))

(defn common-keys
  "This finds the common keys using set/intersection 
   of two maps. Both maps must be un-nested"
  [m1 m2]
  (intersection (set (keys m1)) (set (keys m2))))

(defn is-pattern-match
  "This method accepts two maps and finds if the first 
  represents a pattern for the second. 
  Generally the pattern map should be un-nested"
  [find-map m]
  (let [ufm (un-nest-map find-map)
        um (un-nest-map m)
        ck (common-keys ufm um)]
    (nil?
     (some false?
           (map
            (fn [k]
              ((complement nil?)
               (re-find
                (re-pattern (str "(?i)" (k ufm)))
                (k um))))
            (common-keys ufm um))))))
