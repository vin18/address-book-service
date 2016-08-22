(ns address-book.address-book-store-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [cheshire.core :refer :all]
            [address-book.address-book-store :refer :all]
            [clojure.core.async :as a :refer [go chan >! <!!]]))

(def test-chan (chan 2))

(defn setup []
  (println "Setup.."))

(defn teardown []
  (do
    (reset-db)
    (println "Teardown:: Resetting db / (atom)")))

(defn address-book-store-test-fixture [f]
  (setup)
  (f)
  (teardown))

(use-fixtures :once address-book-store-test-fixture)

(deftest db-store
  (testing "verify delete is done only once"
    (let [ids (:id (first (add-in-db! {:name "A" :email "B"})))]
      (is (= (first (get-db-snapshot)) {(keyword ids) {:name "A", :email "B"}}))
      (go (>! test-chan (try (delete-from-db! (keyword ids)) (catch Exception e (.getMessage e)))))
      (go (>! test-chan (try (delete-from-db! (keyword ids)) (catch Exception e (.getMessage e)))))
      (let [result [(<!! test-chan) (<!! test-chan)]]
        (is (or (= result [[{} nil] [nil "ID Not Found"]]) (= result [[nil "ID Not Found"] [{} nil]]))))))

  (testing "verify that db should have unique name and email"
    (do
      (try (add-in-db! {:name "A" :email "B"}) (catch Exception e :default))
      (try (add-in-db! {:name "A" :email "B"}) (catch Exception e :default)))
    (is (= 1 (count (first (get-db-snapshot))))))

  (testing "verify db entry is updated identified by the id"
    (let [ids (:id (first (add-in-db! {:name "C" :email "D"}))) id (keyword ids)]
      (edit-data-in-db! id {:name "D" :email "C"})
      (is (= (first (get-from-db id)) {:name "D" :email "C"})))))
