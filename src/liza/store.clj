(ns liza.store
  (:require [clojure.core :as core]
            [liza.store.counters :as counter])
  (:refer-clojure :exclude [get put merge]))

(defprotocol Bucket
  "A protocol for abstracting basic key value interactions"
  (get [b k])
  (put [b k v]))

(defprotocol MergeableBucket
  "A protocol for abstracting buckets that inherently want some kind of merge operation.
   riak based buckets are a good example."
  (merge [b v1 v2]))

(defprotocol DeleteableBucket
  "A protocol for buckets that let you delete objects by key"
  (delete [b k]))

(defprotocol Wipeable
  "A protocol for buckets that let you empty the contents. Mostly useful for testing"
  (wipe [b]))

(defprotocol ModifiableBucket
  "A protocol for customized `modify` functions on bucket.
  `modify`
  modifies the data at a key in bucket using the supplied function.

  f should take one argument, which is either nil (when the key isn't there)
  or the existing value at that key.
  A default implementation based on get/put is given for buckets that
  don't customize this"
  (modify [b k f]))

(extend-type java.lang.Object
  ModifiableBucket
  (modify [bucket k f]
    (->>
      (get bucket k)
      (f)
      (put bucket k))))

(defn put-with-merge [bucket k v default]
  (modify bucket k
          (fn [existing]
            (merge bucket (or existing default) v))))

(defn safe-get [^Object bucket k]
  (if-let [result (get bucket k)]
    result
    (throw (RuntimeException. (str "couldn't find key " (class k) ": " k " when trying to get from bucket " (.toString bucket))))))

(deftype InMemoryBucket [bucket-atom merge-fn]
  Bucket
  (get [b k]
    (core/get @bucket-atom k))

  (put [b k v]
    (swap! bucket-atom #(assoc %1 k v)))

  MergeableBucket
  (merge [b v1 v2]
    (merge-fn v1 v2))

  DeleteableBucket
  (delete [b k]
    (swap! bucket-atom #(dissoc %1 k)))

  counter/CounterBucket
  (counter/get-count [b k]
    (core/get @bucket-atom k 0))

  (counter/increment [b k n]
    (swap! bucket-atom
           (fn [m]
             (assoc m
                    k
                    (+ (core/get m k 0) n))))))

(defn in-memory-store
  ([]
   (InMemoryBucket. (atom {}) #(throw (RuntimeException. "no merge fn given to this bucket"))))
  ([merge-fn]
   (InMemoryBucket. (atom {}) merge-fn)))

(extend-type InMemoryBucket
  Wipeable
  (wipe [b]
    (reset! (.bucket-atom b) {})))
