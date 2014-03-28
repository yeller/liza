(ns liza.store.counters
  (:refer-clojure :exclude [get]))

(defprotocol CounterBucket
  (get-count [b k])
  (increment [b k n]))
