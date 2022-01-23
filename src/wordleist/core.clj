(ns wordleist.core
  (:require [clojure.java.io :refer [reader resource]]
            [clojure.string :refer [split-lines]]))

(def word-length 5)

(def words
  (split-lines (slurp (resource "sgb-words.txt"))))

(defn check-length [target name]
  (when-not (= (count target) word-length)
    (throw (Exception. (str name " must have exactly " word-length " letters; '"
                            target "' has " (count target)))))
  true)

(defn member?
  "true if `target` is a member of `collection`, else false."
  [target collection]
  (some #(= target %) collection))

(defn wordle
  "given this `target` as the word saught, return the wordle pattern for this
  `guess`."
  [target guess]
  (check-length target "target")
  (check-length guess "guess")
  (map
   #(let [seeking (nth guess %)]
      (cond
        (= (nth target %) seeking) [:found seeking]
        (some (fn [x] (= seeking x)) target) [:present seeking]
        :else [:not-present seeking]))
   (range (count target))))

(defn wordle-gen
  "Generate a new wordle function, whose target is a five letter word chosen 
   at random from the `words` list"
  []
  (partial wordle (rand-nth words)))

(defn consistent?
  "Where `pattern` is a pattern as returned by `wordle`, and `target` is a
   string representing a five letter word, return `true` it `target` is
   consistent with `pattern`, else false."
  [pattern target]
  (every?
   true?
   (map #(let [present (some (fn [x] (= (nth %1 1) x)) target)]
           (case (first %1)
             :found (= (nth %1 1) %2)
             (:present :not-present) present))
        pattern
        target)))

;; OK, filtering the full list of words for consistency with patterns is very cheap:
;; wordleist.core=> (def p (wordle "water" "aertw"))
;; #'wordleist.core/p
;; wordleist.core=> (time (filter #(when (consistent? p %) %) words))
;; "Elapsed time: 0.114842 msecs"
;; ("water")
;; so it's probably worth filtering the candidate list after each attempt


