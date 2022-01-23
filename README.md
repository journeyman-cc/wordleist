# wordleist

A Clojure library designed to generate and solve wordle-style puzzles.

## Usage

Core functions

* `(wordle target guess)`: given this `target` as the word sought, return the wordle pattern for this
  `guess`.
* `(wordle-gen)`: return a new wordle function, whose target is a word chosen at random from the word
  list.
* `(consistent? pattern target)`: Where `pattern` is a pattern as returned by `wordle`, and `target` is a
   string representing a five letter word, return `true` it `target` is
   consistent with `pattern`, else false.

## License

Copyright © 2022 Simon Brooke (simon@jouneyman.cc)

This program and the accompanying materials are made available under the
terms of the GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
