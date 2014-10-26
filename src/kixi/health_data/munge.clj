(ns kixi.health-data.munge
  (:require [clojure.string :as string]
            [taoensso.timbre :as log]))

(defn trim-all [row]
  (->> (map string/trim row)
       (filter not-empty)))

(defn keyword-header [header-row]
  (->> header-row
       (map string/trim)
       (filter not-empty)
       (map #(string/replace % #" " "_"))
       (map string/lower-case)
       (map keyword)
       vec))

(defn per-capita
  "Returns ratios of count over population"
  [num population]
  (if (and (number? num) (number? population) (< 0 population))
    (/ num population)
    nil))
