(ns wordleist.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [wordleist.core :refer [consistent? with-mark wordle]]))

(deftest consistency
  (testing "consistent"
    (is (consistent? (wordle "water" "water") "water"))
    (is (consistent? (wordle "water" "watre") "water"))
    (is (consistent? (wordle "water" "aertw") "water"))
  ;;  (is (not (consistent? (wordle "water" "watch") "water")))
    ))

(deftest homework
  (testing "marking-waste"
    (let [patterns-waste '(([:found \w] [:found \a] [:not-present \s] [:present \t] [:present \e]))]
      (is (= (with-mark patterns-waste :found) 
             '(\w \a nil nil nil)))))
  (testing "marking-later"
    (let [patterns-later '(([:not-present \l] [:found \a] [:found \t] [:found \e] [:found \r]))]
      (is (= (with-mark patterns-later :found)
             '(nil \a \t \e \r)))))
  (testing "marking-water"
    (let [patterns-water '(([:found \w] [:found \a] [:not-present \s] [:present \t] [:present \e])
                           ([:not-present \l] [:found \a] [:found \t] [:found \e] [:found \r]))]
      (is (= (with-mark patterns-water :found)
             '(\w \a \t \e \r))))))
