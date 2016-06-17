(ns cascalog-check.core
  (:require [cascalog.logic.ops :as c]
            [cascalog.cascading.stats :as stats]
            [cascalog.cascading.io :as io]
            [cascalog.logic.def :as d]
            [cascalog.cascading.def :as cd]
            [cascalog.api :refer :all]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(defn run<-
  "A simple way to run a subquery (a query that starts with <- instead of ??<-)."
  ([query]
   (first (??- query)))
  ([queryfn & args]
   (run<- (apply queryfn args))))

(defn s [x] x)


(def  cat-tap [["foo" 1 3]
               ["foo" 2 100]
               ["foo" 3 8]
               ["foo" 4 12]
               ["bar" 1 5]
               ["bar" 2 22]
               ["bar" 3 36]
               ["bar" 4 212]
               ["baz" 1 3]
               ["baz" 2 100]
               ["baz" 3 8]
               ["baz" 4 300]])

(def foo-catv '([3 100 8 12]))
(def foo-baz-catv '([3 100 8]))
(def foo-bar-catv '([3 100 8 12] [5 22 36 212]))
(def foo-bar-baz-catv '([3 100 8] [5 22 36 212]))

(defn lf [max lvl] (< lvl max))

(defn lvl-f
  [max]
  (filterfn [lvl] (lf max lvl)))

(defn codes [max]
  (let [lvl-filter (lvl-f max)]
    (??<- [?id ?lvl ?code]
          (cat-tap ?id ?lvl ?code)
          (:sort ?lvl)
                                        ;(< ?lvl max)
                                        ;(lf max ?lvl)
          (lvl-filter ?lvl)
          )))

(defn catv-filter
  "returns true or false if a vector matches the categories or not.
  categoriey can be nil or a lists of category vectors.
  The vector will be shortened to match the comparison
  category vectors as needed so that sub categories will match. "
  [categories v]
  (if (empty? categories)
    true
    (reduce #(or (= (vec (take (count %2) v)) %2) %1) false categories)))

(defn catv-f
  "Returns a function to filter vectors by a set of categories."
  [categories]
  (filterfn [v]
            (catv-filter categories v)))




(defbufferfn mk-vec [tuples]
  [[(reduce #(conj %1 %2) [] (map first tuples))]])

(defbufferfn dosum [tuples] [(reduce + (map first tuples))])



(defn cats-sub []
  (<- [?id ?catv]
      (cat-tap ?id ?lvl ?code)
      (:sort ?lvl)
      (mk-vec ?code :> ?catv)))

(defn cats [s]
  (let [prods (cats-sub)
        catf (catv-f s)]
    (??<- [?id ?catv ?res]
          (prods ?id ?catv)
          (catv-filter s ?catv :> ?res)
          (catf ?catv))))

(defn filter-test [s]
  (let [f (catv-f s)]
    (filter #(f (second %)) (run<- (cats-sub)))))

(defn test-them []
  {:cats (cats nil)
   :allcats (filter-test nil)
   :foo (filter-test foo-catv)
   :foo-baz (filter-test foo-baz-catv)})
