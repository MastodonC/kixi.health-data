(ns kixi.health-data.gp-prescriptions-test
  (:require [kixi.health-data.gp-prescriptions :refer :all]
            [clojure.test :refer :all]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [kixi.health-data :as hd]))

(def csv-rows (csv/read-csv (slurp (io/file (io/resource "test/test-pdpi.csv")))))

(def test-hypnotics
  [{:bnf_code {:bnf_paragraph "02", :bnf_section "01", :chemical "K0", :bnf_subparagraph "0", :bnf_code "0401020K0AAAHAH", :product "AA", :strength "AH", :bnf_chapter "04", :equivalent "AH"},
    :pct "RXA", :nic 73.52, :bnf_name "Diazepam_Tab 2mg", :practice "N81646", :period "201406", :quantity 2573.0, :items 28, :sha "Q44", :act_cost 76.01}
   {:bnf_code {:bnf_paragraph "02", :bnf_section "01", :chemical "K0", :bnf_subparagraph "0", :bnf_code "0401020K0AAAHAH", :product "AA", :strength "AH", :bnf_chapter "04", :equivalent "AH"},
    :pct "RXA", :nic 2.74, :bnf_name "Diazepam_Tab 2mg", :practice "Y00327", :period "201406", :quantity 96.0, :items 12, :sha "Q44", :act_cost 3.88}])

(deftest grep-diazepam-2mg-test
  (testing "Extract the hyptnotics (BNF Section 4, Chapter 1, Paragraph 2, Strength AH), or 2mg Diazepam from the test csv"
    (is (= test-hypnotics
           (grep-by-bnf {:bnf_chapter "04" :bnf_section "01" :bnf_paragraph "02" :strength "AH"} csv-rows)))))

(deftest sum-diazepam-2mg-test
  (testing "Get the drug with the most prescribed items."
    (is (= {"0401020K0AAAHAH" {:count 40, :bnf_name "Diazepam_Tab 2mg"}}
           (->> csv-rows
                (grep-by-bnf {:bnf_chapter "04" :bnf_section "01" :bnf_paragraph "02" :strength "AH"})
                sum-items)))))

(deftest top-4-items-test
  (testing "Get the 2 most popular items from the test dataset"
    (is (= [["0401020K0AAAHAH" {:count 40, :bnf_name "Diazepam_Tab 2mg"}]
            ["0401010Z0AAAAAA" {:count 12, :bnf_name "Zopiclone_Tab 7.5mg"}]
            ["0401020K0AAAIAI" {:count 5, :bnf_name "Diazepam_Tab 5mg"}]
            ["0401010ADBBAAAA" {:count 2, :bnf_name "Circadin_Tab 2mg M/R"}]]
           (->> csv-rows
                (grep-by-bnf {:bnf_chapter "04" :bnf_section "01"})
                sum-items
                (topk-items 4))))))

(def chemical-sums
  {{:chemical "P0",:bnf_subparagraph "0",:bnf_paragraph "02",:bnf_section "01",:bnf_chapter "04"} {:count 1, :bnf_names #{"Lorazepam_Tab 1mg"}},
   {:chemical "E0",:bnf_subparagraph "0",:bnf_paragraph "02",:bnf_section "01",:bnf_chapter "04"} {:count 1, :bnf_names #{"Chlordiazepox HCl_Cap 5mg"}},
   {:chemical "T0",:bnf_subparagraph "0",:bnf_paragraph "01",:bnf_section "01",:bnf_chapter "04"} {:count 1, :bnf_names #{"Temazepam_Tab 10mg"}},
   {:chemical "AD",:bnf_subparagraph "0",:bnf_paragraph "01",:bnf_section "01",:bnf_chapter "04"} {:count 2, :bnf_names #{"Circadin_Tab 2mg M/R"}},
   {:chemical "K0",:bnf_subparagraph "0",:bnf_paragraph "02",:bnf_section "01",:bnf_chapter "04"} {:count 46,:bnf_names #{"Diazepam_Tab 5mg" "Diazepam_Tab 2mg" "Diazepam_Tab 10mg"}},
   {:chemical "Z0",:bnf_subparagraph "0",:bnf_paragraph "01",:bnf_section "01",:bnf_chapter "04"} {:count 13,:bnf_names #{"Zopiclone_Tab 7.5mg" "Zopiclone_Tab 3.75mg"}}})

(deftest sum-chemicals-test
  (testing "Sum the number of items dispensed by chemical"
    (is (= chemical-sums
           (->> csv-rows
                (grep-by-bnf {:bnf_chapter "04" :bnf_section "01"})
                sum-chemicals)))))


(def top-chemical-sums
  [[{:chemical "B0",:bnf_subparagraph "0",:bnf_paragraph "01",:bnf_section "03",:bnf_chapter "04"} {:count 76,:bnf_names #{"Amitriptyline HCl_Tab 25mg" "Amitriptyline HCl_Tab 50mg"}}]
   [{:chemical "K0",:bnf_subparagraph "0",:bnf_paragraph "02",:bnf_section "01",:bnf_chapter "04"} {:count 46,:bnf_names #{"Diazepam_Tab 5mg" "Diazepam_Tab 2mg" "Diazepam_Tab 10mg"}}]])

(deftest top-2-chemicals-test
  (testing "Get the top 2 chemicals"
    (is (= top-chemical-sums
           (->> csv-rows
                (grep-by-bnf {:bnf_chapter "04"})
                sum-chemicals
                (topk-items 2))))))

(deftest sum-surgeries-test
  (testing "Sum drugs by surgery"
    (is (= {"Y00327" 27,
            "N81646" 37}
           (->> csv-rows
                (grep-by-bnf {:bnf_chapter "04" :bnf_section "01"})
                sum-surgeries)))))

(deftest top-surgery-test
  (testing "Find the top surgery for a drug type"
    (is (= [["N81646" 37]]
           (->> csv-rows
                (grep-by-bnf {:bnf_chapter "04" :bnf_section "01"})
                sum-surgeries
                (topk-surgeries 1))))))
