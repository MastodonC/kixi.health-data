(ns kixi.health-data
  (:require [clojure.data.csv :as csv]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [taoensso.timbre :as log]
            [com.stuartsierra.frequencies :as freq]
            [kixi.health-data.gp-prescriptions :as scrips]
            [kixi.health-data.gp-practice-counts :as counts]
            [kixi.health-data.munge :as munge]))


(defn surgery-per-capita [scrips practice-counts]
  (let [scrip-counts (scrips/sum-surgeries scrips)]
    (->> scrip-counts
         (map #(vector (first %)
                       (munge/per-capita (second %) (-> practice-counts (get (first %)) :total_all))))
         (remove #(nil? (second %))))))
