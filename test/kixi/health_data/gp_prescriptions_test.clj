(ns kixi.health-data.gp-prescriptions-test
  (:require [kixi.health-data.gp-prescriptions :refer :all]
            [clojure.test :refer :all]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [kixi.health-data :as hd]))

(def csv-rows (csv/read-csv (slurp (io/file (io/resource "test/test-pdpi.csv")))))

(def test-hypnotics
  [{:bnf_code {:bnf_paragraph "02", :bnf_section "01", :chemical "K0", :bnf_subparagraph "0", :bnf_code "0401020K0AAAHAH", :product "AA", :strength "AH", :bnf_chapter "04", :equivalent "AH"},
    :pct "00K", :nic 21.41, :bnf_name "Diazepam_Tab 2mg", :practice "A81001", :period "201406", :quantity 749.0, :items 27, :sha "Q45", :act_cost 22.52}])

(deftest grep-diazepam-2mg-test
  (testing "Extract the hyptnotics (BNF Chapter 4, Section 1, Paragraph 2, Strength AH), or 2mg Diazepam from the test csv"
    (is (= test-hypnotics
           (grep-by-bnf {:bnf_chapter "04" :bnf_section "01" :bnf_paragraph "02" :strength "AH"} csv-rows)))))

(deftest sum-diazepam-2mg-test
  (testing "Get the drug with the most prescribed items."
    (is (= {"0401020K0AAAHAH" {:count 27, :bnf_name "Diazepam_Tab 2mg"}}
           (->> csv-rows
                (grep-by-bnf {:bnf_chapter "04" :bnf_section "01" :bnf_paragraph "02" :strength "AH"})
                sum-items)))))

(deftest top-4-items-test
  (testing "Get the 2 most popular items from the test dataset"
    (is (= [["0401010T0AAAMAM" {:count 86, :bnf_name "Temazepam_Tab 10mg"}]
            ["0401020K0AAAIAI" {:count 43, :bnf_name "Diazepam_Tab 5mg"}]
            ["0401010R0AAACAC" {:count 36, :bnf_name "Nitrazepam_Tab 5mg"}]
            ["0401010T0AAANAN" {:count 31, :bnf_name "Temazepam_Tab 20mg"}]]
           (->> csv-rows
                (grep-by-bnf {:bnf_chapter "04" :bnf_section "01"})
                sum-items
                (topk-items 4))))))

(def chemical-sums
  {{:chemical "P0",:bnf_subparagraph "0",:bnf_paragraph "01",:bnf_section "01",:bnf_chapter "04"} {:count 2, :bnf_names #{"Lormetazepam_Tab 1mg"}},
   {:chemical "K0",:bnf_subparagraph "0",:bnf_paragraph "02",:bnf_section "01",:bnf_chapter "04"} {:count 70, :bnf_names #{"Diazepam_Tab 5mg" "Diazepam_Tab 2mg"}},
   {:chemical "Z0",:bnf_subparagraph "0",:bnf_paragraph "01",:bnf_section "01",:bnf_chapter "04"} {:count 38,:bnf_names #{"Zopiclone_Tab 7.5mg" "Zopiclone_Tab 3.75mg"}},
   {:chemical "Y0",:bnf_subparagraph "0",:bnf_paragraph "01",:bnf_section "01",:bnf_chapter "04"} {:count 15,:bnf_names #{"Zolpidem Tart_Tab 10mg" "Zolpidem Tart_Tab 5mg"}},
   {:chemical "T0",:bnf_subparagraph "0",:bnf_paragraph "01",:bnf_section "01",:bnf_chapter "04"} {:count 117, :bnf_names #{"Temazepam_Tab 10mg" "Temazepam_Tab 20mg"}},
   {:chemical "R0",:bnf_subparagraph "0",:bnf_paragraph "01",:bnf_section "01",:bnf_chapter "04"} {:count 36, :bnf_names #{"Nitrazepam_Tab 5mg"}},
   {:chemical "F0",:bnf_subparagraph "0",:bnf_paragraph "01",:bnf_section "01",:bnf_chapter "04"} {:count 4, :bnf_names #{"Clomethi_Cap 192mg"}},
   {:chemical "AD",:bnf_subparagraph "0",:bnf_paragraph "01",:bnf_section "01",:bnf_chapter "04"} {:count 6,:bnf_names #{"Melatonin_Cap 3mg" "Melatonin_Tab 2mg M/R" "Melatonin_Cap 5mg"}}})

(deftest sum-chemicals-test
  (testing "Sum the number of items dispensed by chemical"
    (is (= chemical-sums
           (->> csv-rows
                (grep-by-bnf {:bnf_chapter "04" :bnf_section "01"})
                sum-chemicals)))))


(def top-chemical-sums
  [ [{:chemical "T0",:bnf_subparagraph "0",:bnf_paragraph "01",:bnf_section "01",:bnf_chapter "04"} {:count 117,:bnf_names #{"Temazepam_Tab 10mg" "Temazepam_Tab 20mg"}}]
    [{:chemical "K0",:bnf_subparagraph "0",:bnf_paragraph "02",:bnf_section "01",:bnf_chapter "04"} {:count 70, :bnf_names #{"Diazepam_Tab 5mg" "Diazepam_Tab 2mg"}}]])

(deftest top-2-chemicals-test
  (testing "Get the top 2 chemicals"
    (is (= top-chemical-sums
           (->> csv-rows
                (grep-by-bnf {:bnf_chapter "04"})
                sum-chemicals
                (topk-items 2))))))

(deftest sum-surgeries-test
  (testing "Sum drugs by surgery"
    (is (= {"A81002" 120,
            "A81001" 168}
           (->> csv-rows
                (grep-by-bnf {:bnf_chapter "04" :bnf_section "01"})
                sum-surgeries)))))

(deftest top-surgery-test
  (testing "Find the top surgery for a drug type"
    (is (= [["A81001" 168]]
           (->> csv-rows
                (grep-by-bnf {:bnf_chapter "04" :bnf_section "01"})
                sum-surgeries
                (topk-surgeries 1))))))
