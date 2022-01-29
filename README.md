# wordleist

A Clojure library designed to generate and solve wordle-style puzzles.

## Status

This works reasonably well. It doesn't have a client interface to the real Wordle server; that coulf be done but it wasn't my objective.

It solves 94% of wordles in random tests in 6 or fewer attempts; the distribution of attempts in testing is:

```clojure
wordleist.core=> (frequencies (map :attempts s))
{1 1, 
2 37, 
3 30, 
nil 6, ;; these are the failures
4 18, 
5 6, 
6 2}
```

## Usage

Core functions

* `(wordle target guess)`: given this `target` as the word sought, return the wordle pattern for this
  `guess`.
* `(wordle-gen)`: return a new wordle function, whose target is a word chosen at random from the word
  list.
* `(consistent? pattern target)`: Where `pattern` is a pattern as returned by `wordle`, and `target` is a
   string representing a five letter word, return `true` it `target` is
   consistent with `pattern`, else false.
* `(solve game)`: Solves a wordle game, and returns the solution.

Example:

```clojure
wordleist.core=> (pprint (solve (wordle-gen)))
{:word "young",
 :attempts 5,
 :patterns
 (([:not-present \s]
   [:not-present \e]
   [:not-present \a]
   [:present \o]
   [:not-present \r])
  ([:not-present \w]
   [:found \o]
   [:found \u]
   [:not-present \l]
   [:not-present \d])
  ([:not-present \c]
   [:found \o]
   [:found \u]
   [:not-present \l]
   [:not-present \d])
  ([:not-present \f]
   [:found \o]
   [:found \u]
   [:found \n]
   [:not-present \d])
  ([:found \y] [:found \o] [:found \u] [:found \n] [:found \g]))}
```

## Other implementations

[Joseph Fahey](http://josf.info/) has an implementation [here](https://github.com/josf/guess-lisp), which has a much more sophisticated guess generation phase tham mine (yet) has.

## License

Copyright © 2022 Simon Brooke (simon@jouneyman.cc)

This program and the accompanying materials are made available under the
terms of the GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
