(ns wordleist.core-test
  (:require [clojure.test :refer :all]
            [wordleist.core :refer :all]))

(deftest consistency
  (testing "consistent"
    (is (consistent? (wordle "water" "water") "water"))
    (is (consistent? (wordle "water" "watre") "water"))
    (is (consistent? (wordle "water" "aertw") "water"))
    (is (not (consistent? (wordle "water" "watch") "water")))
    ))
