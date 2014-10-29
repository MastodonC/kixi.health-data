(ns kixi.health-data.ccg
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [taoensso.timbre :as log]
            [kixi.health-data.munge :as munge]))

(def final-ccg-rca-header [:ccg_code :ccg_name :registered_population_2012 :registered_population_adjusted_to_new_ons13_projections :commissioning_board_region :current_pct_cluster :running_cost_allowance_gbp_m])

(defn ccg-names
  ([filename]
     (with-open [rdr (io/reader (io/resource filename))]
       (->> (csv/read-csv rdr)
            (drop 1)
            (map #(zipmap final-ccg-rca-header %))
            (map #(vector (:ccg_code %) %))
            (into {}))))
  ([]
     (ccg-names "reference/final-ccg-rca.csv")))
