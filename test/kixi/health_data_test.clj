(ns kixi.health-data-test
  (:require [clojure.test :refer :all]
            [kixi.health-data :refer :all]
            [kixi.health-data.gp-practice-counts :as counts]
            [kixi.health-data.gp-prescriptions :as scrips]
            [clojure.java.io :as io]))

(deftest per-capita-test
  (testing "Drugs ratios at a surgery."
    (is (= [["A81002" 20/3259] ["A81001" 28/697]]
           (process-csv
            (fn [rows]
              (let [surgery-counts (counts/gp-practice-counts "test/gp-practice-counts.csv")]
                (->> rows
                     (scrips/grep-by-bnf {:bnf_chapter "04" :bnf_section "01"})
                     (surgery-per-capita surgery-counts)
                     vec)))
            (io/file (io/resource "test/test-pdpi.csv")))))))
