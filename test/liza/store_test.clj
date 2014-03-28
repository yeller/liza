(ns liza.store-test
  (:use clojure.test)
  (:require [liza.store :as store :reload true]
            [liza.store.counters :as counter]))

(deftest store-tests
  (testing "get gets back what is put"
    (let [b (store/in-memory-store)]
      (store/put b "key" "value")
      (is (= "value" (store/get b "key")))))

  (testing "modify changes an existing value"
    (let [b (store/in-memory-store)]
      (store/put b "key" 1)
      (store/modify b "key" inc)
      (is (= 2 (store/get b "key")))))

  (testing "modify on a non existing value gives nil"
    (let [b (store/in-memory-store)]
      (store/modify b "key" nil?)
      (is (= true (store/get b "key")))))

  (testing "put-with-merge before a value is there uses the default"
    (let [b (store/in-memory-store +)]
      (store/put-with-merge b "key" 1 0)
      (is (= 1 (store/get b "key")))))

  (testing "put-with-merge merges a new value onto the previous one"
    (let [b (store/in-memory-store +)]
      (store/put b "key" 1)
      (store/put-with-merge b "key" 1 0)
      (is (= 2 (store/get b "key")))))

  (testing "getting an empty counter returns 0"
    (let [b (store/in-memory-store)]
      (is (= 0 (counter/get-count b "key")))))

  (testing "getting a counter after incrementing returns the value"
    (let [b (store/in-memory-store)]
      (counter/increment b "key" 1)
      (is (= 1 (counter/get-count b "key"))))))
