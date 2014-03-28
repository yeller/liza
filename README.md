# liza

> There's a hole in my bucket, dear liza

A clojure library for abstracting key value storage, based around individual buckets

## Goals / Design

Liza has some specific goals:

- tiny core

The core of liza, if you ignore the in memory store, is 51 lines of code, all
told. Bucket implementations are often larger than this.

- bucket based

liza wants you to break up storage in terms of buckets - you should mostly only
be storing the same kinds of things in each bucket, but you might have many
buckets from the same storage type. By basing things around buckets, things like
serialization, caching, metrics and eventually consistent merge can be done per
kind of data you put in storage, and clients don't have to think about them at
all.

- bucket implementations handle serialization themselves

Some storage types (in particular riak), benefit from being able to handle
serialization themselves (if the bucket handles its own serialization, then
your merge functions can be hidden from users of the bucket, and the merge
functions can operate on deserialized data).

- bucket implementations handle consistency themselves

Basic get/put usage can be painful with concurrent usage. To solve this, use
the `ModifiableBucket` protocol, and have your bucket do some sort of CAS or
vector clock based consistency around that. There's a default implementation
for `ModifiableBucket`, but most buckets should override this themselves.

## Usage

liza's core is tiny - 5 protocols that abstract all I've wanted to do with KV stores. The protocols are easiest as source, so here they are:

```clojure
(defprotocol Bucket
  (get [b k])
  (put [b k v]))

(defprotocol MergeableBucket
  (merge [b v1 v2]))

(defprotocol DeleteableBucket
  (delete [b k]))

(defprotocol Wipeable
  (wipe [b]))

(defprotocol ModifiableBucket
  (modify [b k f]))
```

Note that some of these function names clash with clojure.core. I nearly always
`(require [liza.store :as store])` to get around this.

This library, the core doesn't ship with any implementations, except a super dumb in memory one that is used in tests.

There's also a protocol for abstracting stores that have special counter implementations:

```clojure
(defprotocol CounterBucket
  (get-count [b k])
  (increment [b k n]))
```

Many stores provide interesting implementations of counters, this lets you
interact with them. For now, counters only support increment and read.

## License

Copyright © 2013 Tom Crayford

Distributed under the Eclipse Public License, the same as Clojure.
