(ns ^:export wordleist.core
  "Generate and solve wordle-style puzzles."
  (:require #?(:clj  [clojure.java.io :refer [resource]]
               :cljs [shadow.resource :refer [inline]])
            #?(:clj [hiccup.core :refer [html]]
               :cljs [crate.core :refer [html]])
            [clojure.string :refer [join split-lines]]))

(def word-length
  "The length of the words we operate on."
  5)

#?(:cljs (def sgb-words
           "Full text of the `sgb-words.txt` file, inlined in ClojureScript only, since
            in pure ClojureScript we have no filesystem."
           (inline "sgb-words.txt")))

(def freq
  "Frequency-sorted list of letters in the candidate words."
  (let [f (dissoc (frequencies #?(:clj (slurp (resource "sgb-words.txt"))
                                  :cljs sgb-words)) \newline)]
    (sort #(> (f %1) (f %2)) (keys f))))

(def words
  "The candidate words."
  (cons
   (apply str (take 5 freq)) ;; prepend a fake word comprising the five most frequent letters
   (split-lines #?(:clj (slurp (resource "sgb-words.txt"))
                   :cljs sgb-words))))

(defn check-length
  "Check that this `target` is exactly `word-length` long, and throuw an exception labelled
   with `name` if it is not."
  [target name]
  (when-not (= (count target) word-length)
    (throw (#?(:clj Exception.
               :cljs js/Error.) (str name " must have exactly " word-length " letters; '"
                                     target "' has " (count target)))))
  true)

(defn member?
  "true if `target` is a member of `collection`, else false."
  [target collection]
  (some #(= target %) collection))

(defn wordle
  "given this `target` as the word sought, return the wordle pattern for this
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

(defn ^:export wordle-gen
  "Generate a new wordle function, whose target is a five letter word chosen 
   at random from the `words` list"
  []
  (partial wordle (rand-nth words)))

(defn- in-consistent
  "Internal guts of `consistent?`, q.v."
  [pattern target]
  (map #(let [present (some (fn [x] (= (nth %1 1) x)) target)]
          (case (first %1)
            :found (= (nth %1 1) %2)
            :present present
            :not-present (not present)
            :not-found true))
       pattern
       target))

(defn consistent?
  "Where `pattern` is a pattern as returned by `wordle`, and `target` is a
   string representing a five letter word, return `true` it `target` is
   consistent with `pattern`, else false."
  [pattern target]
  (every?
   true?
   (in-consistent
    pattern
    target)))

;; OK, filtering the full list of words for consistency with patterns is very cheap:
;; wordleist.core=> (def p (wordle "water" "aertw"))
;; #'wordleist.core/p
;; wordleist.core=> (time (filter #(when (consistent? p %) %) words))
;; "Elapsed time: 0.114842 msecs"
;; ("water")
;; so it's probably worth filtering the candidate list after each attempt

(defn ignore-not-present
  "Replace all occurances of :not-present in this `pattern` with :not-found."
  [pattern]
  (map
   #(if
     (= :not-present (first %)) [:not-found (nth % 1)]
     %)
   pattern))

(defn refine-candidates
  "Refine these `candidates` by removing those which are not consistent with this `pattern`,
   generated from this `word`."
  ([candidates pattern]
   (refine-candidates candidates
                      (apply str (map #(nth % 1) pattern))
                      pattern))
  ([candidates word pattern]
   (filter
    #(when (consistent?
            (if (member? word candidates)
              pattern
              (ignore-not-present pattern))
            %) %)
    candidates)))

(defn pattern?
  "Validate wordle patterns. Return `true` if `pattern` is a valid pattern, else `false`.

   A pattern is a sequence of `word-length` sequences, such that:
   1. every element is a sequence, or nil;
   2. the first element of every sequence which is not nil is a keyword;
   3. the second element of every sequence which is not nil is a character."
  [pattern]
  (and (seq? pattern)
       (= (count pattern) word-length)
       (every? true?
               (map #(cond
                       (nil? %) true
                       (and (seq %) (keyword? (first %)) (char? (nth % 1))) true
                       :else false)
                    pattern))))

(defn with-mark
  "Return a list of `word-length` elements, comprising all those characters
   which are marked `mark` in any of these patterns."
  [patterns mark]
  (when-not
   (every? pattern? patterns)
    (throw (#?(:clj Exception.
               :cljs js/Error.) (str "Not a pattern: " patterns))))
  (reduce
   (fn [s1 s2]
     (map #(if (char? (nth s1 %)) (nth s1 %) (nth s2 %))
          (range word-length)))
   (repeat word-length (vector))
   (map
    (fn [p]
      (map
       (fn [e]
         (when (= mark (first e))
           (nth e 1)))
       p))
    patterns)))

(defn all-with-mark
  "Return a sequence of indefinite length, containung all characters with 
   this `mark` in these `patterns`.

   As with `generate `which depends on it, this is experimental code which is currently
   not used."
  [patterns mark]
  (remove nil?
          (flatten
           (map
            (fn [p]
              (map
               (fn [e]
                 (when (= mark (first e))
                   (nth e 1)))
               p))
            patterns))))

(defn remove-all
  "Return a sequence like `seq`but with all members of `to-remove` removed.

   As with `generate `which depends on it, this is experimental code which is currently
   not used."
  [to-remove seq]
  (remove #(member? % to-remove) seq))

(defn remove-all-with-mark
  "Return a set of characters like `freq`, but with all marked with this `mark` in
   any of these patterns removed.

   As with `generate `which depends on it, this is experimental code which is currently
   not used."
  [patterns mark]
  (let [np (all-with-mark patterns mark)]
    (remove-all np freq)))

(defn- alternate-possible
  "Where `possibles` is a list of characters which have been marked `possible` in these
   `patterns`, return the first which has never been marked as `possible` in this
   `position`.

   As with `generate`, which depends on it, this is experimental code which is currently
   not used."
  [patterns possibles others position]
  (let [already-tried (remove nil?
                              (map #(let [e (nth % position)]
                                      (when (= :not-present (first e))
                                        (nth e 1)))
                                   patterns))
        possibles'    (remove-all already-tried possibles)]
    (if (empty? possibles')
      (first others)
      (first possibles'))))

(defn generate
  "Generate the next word to test, given these `patterns` from previous tests, 
   and these `candidates` for the word to be found. Note that the best word to 
   test is not necessarily a member of `candidates`.

   TODO: the idea hear was to generate the most efficient possible test strings,
   but it added a lot of complexity and doesn't work well so it is currently not
   used."
  [patterns candidates]
  (let [f  (with-mark patterns :found)
        fs (remove empty? f)
        p  (seq (set (remove-all fs (all-with-mark patterns :present))))
        o  (remove-all-with-mark patterns :not-present)]
    (let [eureka (= (count fs) word-length)]
      {:eureka eureka
       :cand   (apply
                str
                (if eureka
                  fs
                  (map
                   #(cond
                      (nil? (nth f %)) (nth o %) ;; if it's a found position, try the next char we don't know about
                      (> (count p) 1) (alternate-possible patterns p o %)
                      :else (nth o %)) ;; not ideal; probably use `loop` instead of `map` 
                          ;; so we can try others in strict order
                   (range word-length))))})))

(defn ^:export solve
  "Solve a wordle; with no arguments, start with a new random word; otherwise,
   arguments are:
   * `game` a function of one argument, as generated by `wordle-gen`, which takes
     a guessed word as argument and returns a pattern;
   * `patterns` a list of patterns from prior attempts;
   * `candidates` a list of words to use as candidates for guessing."
  ([] (wordle-gen))
  ([game]
   (solve game nil))
  ([game patterns]
   (solve game patterns words))
  ([game patterns candidates]
   (loop [i        0
          to-test  (first candidates)
          patterns patterns
          cands    candidates]
     (let [εὕρηκα (with-mark patterns :found)]
       (cond (> i 6) nil
             (every? char? εὕρηκα) {:word     (apply str εὕρηκα)
                                    :attempts (count patterns)
                                    :patterns (reverse patterns)}
             :else (case (count cands)
                     0 nil ;; fail
                     1 {:word     (first cands)
                        :attempts (count patterns)
                        :patterns (reverse patterns)}
       ;; else
                     (let [pattern   (apply game (list to-test))
                           patterns' (cons pattern patterns)
                           cands'    (refine-candidates cands to-test pattern)]
                       (recur (inc i)
                              (first cands')
                              patterns'
                              (rest cands')))))))))

(defn display-pattern
  "Display one `pattern` from a solution-map as a table row of tiles."
  [pattern]
  [:tr
   (map
    #(vector :td {:class (str "tile " (name (first %)))} (str (nth % 1)))
    pattern)])

(defn ^:export display
  "Display this `solution-map`, as returned by `solve`."
  [solution-map]
  (html
   (if (empty? solution-map)
     [:h1 {:class "fail"} "Failed :-("]
    ;; else
     [:div {:class "wordleist"}
      [:h1 (:word solution-map)]
      [:h4 "Attempts: " (:attempts solution-map)]
      [:table
       (map display-pattern (:patterns solution-map))]])))