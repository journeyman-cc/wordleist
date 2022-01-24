(ns wordleist.to-html
  "Display solutions to wordle-style puzzles."
  (:require #?(:clj [hiccup.core :refer [html]]
               :cljs [crate.core :refer [html]])))

(defn display-pattern
  "Display one `pattern` from a solution-map as a table row of tiles."
  [pattern]
  [:tr
   (map
    #(vector :td {:class (str "tile " (name (first %)))} (str (nth % 1)))
    pattern)])

(defn display
  "Display this `solution-map`, as returned by `solve`."
  [solution-map]
  (html
   (if (empty? solution-map) 
     [:h1 {:class "fail"} "Failed :-("]
    ;; else
     [:div [:class "wordleist"]
      [:h1 (:word solution-map)]
      [:h4 "Attempts: " (:attempts solution-map)]
      [:table
       (map display-pattern (:patterns solution-map))]])))