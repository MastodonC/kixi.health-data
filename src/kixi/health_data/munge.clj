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

;; (freq/stats (freq/bucket-frequencies 0.01 (map second (surgery-per-capita all-antibiotics all-surgeries))))
(defn per-capita [num population]
  (if (and (number? num) (number? population) (< 0 population))
    (float (/ num population))
    nil))
