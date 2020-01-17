(ns cookbook.events.utils)

(defn remove-at [v idx]
  (into
    (subvec v 0 idx)
    (subvec v (inc idx))))

(defn conj-flatten [coll item]
  (vec (flatten (conj coll item))))
