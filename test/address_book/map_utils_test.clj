(ns address-book.map-utils-test
  (:require [clojure.test :refer :all]
            [address-book.map-utils :refer :all]))

(deftest map-utils
  (testing "test un-nesting of maps"
    (is (= {:a 1 :a1 22} (un-nest-map {:a 1 :b {:c {:d {:e {:f {:g {:a1 22}}}}}}})))
    (is (= {:a 1 :b 22} (un-nest-map {:a 1 :b 22})))
    (is (= {:a 1 :c 2 :d 3} (un-nest-map {:a 1 :b {:c 2 :d 3}}))))

  (testing "common-keys"
    (is (= #{:a :b} (common-keys {:a 22 :b 33 :c 44} {:a 212 :b 313 :d 414}))))

  (testing "is pattern match"
    (let [record {:name "Jane Doe"
                  :email "jane@doetech.com"
                  :phone "6502603255"
                  :address {:house "420"
                            :apartment  "Bellview Complex"
                            :city  "San Francisco"
                            :state  "CA"
                            :zip  "94107"}}]
      (is (true? (is-pattern-match {:name  "jane", :house "420"} record)))
      (is (true? (is-pattern-match {:email "jane@doetech.com"} record)))
      (is (false? (is-pattern-match {:name "john"} record)))
      (is (false? (is-pattern-match {:city "jane@doetech.com"} record))))))

