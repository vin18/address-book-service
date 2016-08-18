(ns address-book.db-schema-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [cheshire.core :refer :all]
            [schema.core :as s]
            [address-book.db-schema :refer :all]))

(deftest db-schema
  (testing "valid incoming data"
    (let [incoming-data {:name "AaBb" :email "aabb@gg.com"}
          result (validate-data incoming-data)]
      (is (= result incoming-data))))

  (testing "invalid key in incoming data"
    (let [incoming-data {:naame "AABB"}]
      (is (thrown? Exception (validate-data incoming-data)))))

  (testing "invalid name in incoming data"
    (let [incoming-data {:name "A3434" :email "aabb@gmail.com"}]
      (is (thrown? Exception (validate-data incoming-data)))))

  (testing "valid phone number should be of 10 digits only"
    (let [valid-num "1234567890"
          invalid-nums ["1213" "123qse1110" "12345678901"]]
      (is (= valid-num (s/validate phone-no-pred valid-num)))
      (is (= (map
              (fn [x] (try (s/validate phone-no-pred x) (catch Exception e true)))
              invalid-nums)
             (repeat (count invalid-nums) true)))))

  (testing "valid email"
    (let [valid-emails ["a@g.com" "a232.a22@g.com"]
          invalid-emails ["aa" "aa.com" "@.com" "@gmail.com" "..@.com" "..@gmail.com"]]
      (is (= (map (fn [x] (s/validate email-pred x)) valid-emails) valid-emails))
      (is (= (map
              (fn [x] (try (s/validate email-pred x) (catch Exception e true)))
              (repeat (count invalid-emails) true)))))))

