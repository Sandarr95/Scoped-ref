(ns scoped-ref.atom)

(defprotocol IAssociativeAtom
  (scope [this ks]))

(defrecord ScopedAtom [root path]
  IAssociativeAtom
  (scope [this ks] (ScopedAtom. root (concat path ks)))

  clojure.lang.IDeref
  (deref [this] (get-in @root path))

  clojure.lang.IAtom
  (swap [this f]
    (get-in (swap! root update-in path f) path))
  (swap [this f x]
    (get-in (swap! root update-in path f x) path))
  (swap [this f x y]
    (get-in (swap! root update-in path f x y) path))
  (swap [this f x y args]
    (get-in (apply swap! root update-in path f x y args) path))
  (reset [this new-val]
    (get-in (swap! root assoc-in path new-val) path))
  (compareAndSet [this old-val new-val]
    (compare-and-set! root
      (assoc-in @root path old-val)
      (assoc-in @root path new-val)))

  clojure.lang.IRef
  (setValidator [this f]
    (set-validator! root
      (fn [new-state]
        (f (get-in new-state path)))))
  (getValidator [this] (get-validator root))
  (addWatch [this key f] (do
    (add-watch root {:path path :key key}
      (fn [{:keys [path key]} _ old new]
        (if-not (= (get-in old path) (get-in new path))
          (f key this (get-in old path) (get-in new path)))))
    this))
  (removeWatch [this key]
    (remove-watch root {:path path :key key})))

(defmethod print-method ScopedAtom [v ^java.io.Writer w]
  (print-method [(class v) (hash v) (into {} v)] w))

(extend-type clojure.lang.Atom
  IAssociativeAtom
  (scope [this ks] (ScopedAtom. this ks)))
