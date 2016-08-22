(ns address-book.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [cheshire.core :refer :all]
            [address-book.handler :refer :all]
            [address-book.address-book-store :refer [get-db-snapshot
                                                     get-unique-id]]))

(deftest test-app
  (testing "main route"
    (let [response (app (mock/request :get "/address-book/"))]
      (is (= (:status response) 200))
      (is (= (:body response) "{}"))))

  (testing "add new valid entry [name / email]"
    (let [response (app
                    (mock/body (mock/request :post "/address-book/")
                               "{\"name\" : \"John Doe\", \"email\": \"john.doe@gmail.com\"}"))]
      (is (= (:status response) 201))
      (is (= (:body response) (re-matches #"\{\"id\":\"[a-zA-Z0-9-]+\"\}" (:body response))))))

  (testing "add new invalid entry only :name"
    (let [response (app
                    (mock/body (mock/request :post "/address-book/")
                               "{\"name\" : \"Jane Doe\"}"))]
      (is (= (:status response) 400))
      (is (= (:body response) "Value does not match schema: {:email missing-required-key}"))))

  (testing "add new invalid entry only :email"
    (let [response (app
                    (mock/body (mock/request :post "/address-book/")
                               "{\"email\" : \"jane@doetech.com\"}"))]
      (is (= (:status response) 400))
      (is (= (:body response) "Value does not match schema: {:name missing-required-key}"))))

  (testing "main route again"
    (let [response (app (mock/request :get "/address-book/"))]
      (is (= (:status response) 200))
      (is (= (vals (parse-string (:body response))) '({"name" "John Doe", "email" "john.doe@gmail.com"})))))

  (testing "add new invalid entry with only :name"
    (let [response (app
                    (mock/body (mock/request :post "/address-book/")
                               "{\"name\" : \"Jane Doe\"}"))]
      (is (= (:status response) 400))
      (is (= (:body response) "Value does not match schema: {:email missing-required-key}"))))
  (testing "add new invalid entry with only :email"
    (let [response (app
                    (mock/body (mock/request :post "/address-book/")
                               "{\"email\" : \"jane@doetech.com\"}"))]
      (is (= (:status response) 400))
      (is (= (:body response) "Value does not match schema: {:name missing-required-key}"))))

  (testing "main route again"
    (let [response (app (mock/request :get "/address-book/"))]
      (is (= (:status response) 200))
      (is (= (vals (parse-string (:body response))) '({"name" "John Doe", "email" "john.doe@gmail.com"})))))

  (testing "add new valid entry with id G-001"
    (let [response (with-redefs-fn
                     {#'address-book.address-book-store/get-unique-id (fn [] "G-001")}
                     #(app
                       (mock/body (mock/request :post "/address-book/")
                                  "{\"name\" : \"Jane Doe\", \"email\": \"jane.doe@gmail.com\"}")))]
      (is (= (:status response) 201))
      (is (= (:body response) "{\"id\":\"G-001\"}"))))

  (testing "get from db using a valid id"
    (let [response (app (mock/request :get "/address-book/G-001/"))]
      (is (= (:status response) 200))
      (is (= (:body response) "{\"name\":\"Jane Doe\",\"email\":\"jane.doe@gmail.com\"}"))))

  (testing "get from db using invalid id"
    (let [response (app (mock/request :get "/address-book/G_001"))]
      (is (= (:status response) 404))
      (is (= (:body response) "Not Found"))))

  (testing "search db using a string"
    (is (> (count (get-db-snapshot)) 0))
    (let [response (app (mock/request :get "/address-book/search/com"))]
      (is (= (count (parse-string (:body response))) 2))))

  (testing "search using a map"
    (let [entry "{ \"name\" : \"Jenny Unknown\" , \"email\" : \"jen@doetech.com\", \"phone\":\"6502603255\", \"address\" : {\"house\" : \"420\",\n\"apartment\" : \"Bellview Complex\", \"city\": \"San Francisco\", \"state\": \"CA\", \"zip\": \"94107\"}}"
          add-response (app (mock/body (mock/request :post "/address-book/") entry))
          ids (get (parse-string (:body add-response)) "id")
          search-response (app (mock/body (mock/request :post "/address-book/search/")
                                          "{ \"name\" : \"jenny\",  \"house\" : \"420\"}"))
          get-response (app (mock/request :get (str "/address-book/" ids "/")))]
      (is (= (first (parse-string (:body search-response))) (parse-string (:body get-response))))))

  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404)))))
