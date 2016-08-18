(ns address-book.db-schema
  (:require [schema.core :as s]))

(def alpha-space-pred
  "Defines a predicate to identify pattern which allows only 
   letters and spaces"
  (s/pred #(re-matches #"[a-zA-Z ]+" %)))

(def email-pred
  "Defines a predicate to match valid email ids"
  (s/pred #(re-matches #"[a-zA-Z][a-zA-Z0-9_\.]*\@[a-zA-Z]+\.com" %)))

(def phone-no-pred
  "Defines a predicate to match valid phone numbers"
  (s/pred #(re-matches #"\d{10}" %)))

(def zipcode-pred
  "Defines a predicate to match a valid zip-code"
  (s/pred #(re-matches #"[0-9]+" %)))

(def data-schema
  "A schema for the incoming data"
  {(s/required-key :name) alpha-space-pred
   (s/required-key :email) email-pred
   (s/optional-key :phone) phone-no-pred
   (s/optional-key :address) {(s/optional-key :house) s/Str
                              (s/required-key :apartment) alpha-space-pred
                              (s/required-key :city) alpha-space-pred
                              (s/required-key :state) alpha-space-pred
                              (s/required-key :zip) zipcode-pred}})

(defn validate-data
  "Validates the data according to the data-schema"
  [data]
  (s/validate data-schema data))
