## address-book (Dev Spec)

### Components
- Store 
- Handlers 
- Schema Validator

---
#### Store
_DEPS: cheshire.core_

The store is the in memory db and is implemented as a atom of {}.
##### Adding a new entry in DB
Adding an entry will swap! the value of the atom iff the new data has valid schema and does not violate the DB constraints. Swap will conj the current value of the atom with a gensym generated key and the new-data as value.

##### Deleting from DB
Swap will remove an entry from the map iff the id is present in the current value of the map, else throw an exception.

##### Editing an exsiting entry
Use swap to edit out an entry from the map iff the entry key is present in the map.

##### Searching in DB
There can be two ways to search the DB string-pattern-match or using a map as a pattern. The string match will match the values, without the 'key' constraint. A map match can specifiy which key to map with which value.

For example, the following map search request will result in finding 
```json
{ "name" : "jane",  "house" : "420"}
```

```json
[
  {
    "name": "Jane Doe",
    "email": "jane@doetech.com",
    "phone": "6502603255",
    "address": {
      "house": "420",
      "apartment": "Bellview Complex",
      "city": "San Francisco",
      "state": "CA",
      "zip": "94107"
    }
  }
]
````


---

#### Handlers
_DEPS: compojure.core compojure.route cheshire.core ring.middleware.defaults_

| Routes       | Method          |Status            | Description |
| :------------- |:-------------:|:--------------|:-----|
| `/address-book/`| POST| status 201 / 500| Creates a new entry in DB| 
| `/address-book/`| GET| status 200  | Returns a JSON of all the entries in DB| 
| `/address-book/:id`| GET | status 200 /500| Returns the entry identified by id. |
|`/address-book/:id`| PUT| status 200 / 500 | Edits the entry identified by the id.|
|`/address-book/:id`| DELETE | status 200 / 500 | Deletes the entry identified by the id.|
| `/address-book/search/:sstr`| GET | status 200| Returns a JSON of all the entries which match|
| `/address-book/search/`| POST | status 200| Returns a JSON of all the entries which match|

---
#### Schema Validator
_DEPS: prismatic/schema_

- :name and :email are required keys
- :phone, and address are optional
- :address is again a map with
- [:address :house] is an optional key
- [:address [:apartment :zip :city :state]] are required keys


```
Names, Cities, States and Apartments can only contain letters and spaces
```
```
Phone numbers should only contain 10 digits
```
```
Emails are [alpha-numeric] @ [alpha] .com 
Invalid emails:: "aa" "aa.com" "@.com" "@gmail.com" "..@.com" "..@gmail.com"
```

---


## Running

To start a web server for the application, run:

    lein ring server

To generate documentation, run:

    lein codox
    
To format / lint the changes, run:

    lein cljfmt fix
    
To run the tests, run:

    lein test
