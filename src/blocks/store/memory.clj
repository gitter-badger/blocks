(ns blocks.store.memory
  "Block storage backed by an atom in memory."
  (:require
    [blocks.core :as block]
    [multihash.core :as multihash]))


(defn- block-stats
  "Augments a block with stat metadata."
  [block]
  (assoc block
    :stat/size (block/size block)
    :stat/stored-at (or (:stat/stored-at block)
                        (java.util.Date.))))


;; Block records in a memory store are held in a map in an atom.
(defrecord MemoryBlockStore
  [memory]

  block/BlockStore

  (enumerate
    [this opts]
    (block/select-hashes opts (keys @memory)))


  (stat
    [this id]
    (when-let [block (get @memory id)]
      (dissoc block :content)))


  (get*
    [this id]
    (get @memory id))


  (put!
    [this block]
    (if-let [id (:id block)]
      (or (get @memory id)
          (let [block (block-stats block)]
            (swap! memory assoc id block)
            block))))


  (delete!
    [this id]
    (swap! memory dissoc id))


  (erase!!
    [this]
    (swap! memory empty)))


(defn memory-store
  "Creates a new in-memory block store."
  []
  (MemoryBlockStore. (atom (sorted-map) :validator map?)))
