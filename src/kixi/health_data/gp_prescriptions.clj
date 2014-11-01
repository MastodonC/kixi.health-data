(ns kixi.health-data.gp-prescriptions
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [taoensso.timbre :as log]
            [kixi.health-data.ccg :as ccg]
            [kixi.health-data.ods :as ods]
            [kixi.health-data.munge :as munge]))

(set! *warn-on-reflection* true)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Record Parsing and Queries

;; example
;; [:sha :pct :practice :bnf_code :bnf_name :items :nic :act_cost :quantity :period]

;; This is the BNF code for the drug.

;; The "equivalent" is defined as follows.
;; If the presentation is a generic, the 14th and 15th character will be the same as
;; the 12th and 13th character.
;; Where the product is a brand the 14th and 15th digit will match that of the
;; generic equivalent, unless the brand does not have a generic equivalent in
;; which case A0 will be used.
(defn split-bnf-code [^String code]
  {:bnf_code code
   :bnf_chapter      (.substring code 0 2) ;; Characters 1 & 2 show the BNF Chapter,
   :bnf_section      (.substring code 2 4) ;; 3 & 4 show the BNF Section,
   :bnf_paragraph    (.substring code 4 6) ;; 5 & 6 show the BNF paragraph,
   :bnf_subparagraph (.substring code 6 7) ;; 7 shows the BNF sub-paragraph and
   :chemical         (.substring code 7 9) ;; 8 & 9 show the Chemical Substance.
   :product          (.substring code 9 11) ;; 10 & 11 show the Product
   :strength         (when (= 15 (count code)) (.substring code 11 13)) ;; 12 & 13 show the Strength and Formulation
   :equivalent       (when (= 15 (count code)) (.substring code 13 15)) ;; 14 & 15 show the equivalent
   })

(defn pdpi-record [header row]
  (try
    (let [trimmed-row (munge/trim-all row)
          record      (zipmap header trimmed-row)]
      (assoc
          record
        :bnf_code (split-bnf-code (:bnf_code record))
        :quantity (Double/parseDouble ^String (:quantity record))
        :items (Integer/parseInt ^String (:items record))
        :act_cost (Double/parseDouble ^String (:act_cost record))
        :nic (Double/parseDouble ^String (:nic record))))
    (catch Throwable t
      (log/errorf t "Could not parse %s" row)
      (throw t))))

(defn bnf-match? [bnf-query pdpi-record]
  (let [query-keys (vec (keys bnf-query))
        bnf-sub-map (select-keys (:bnf_code pdpi-record) query-keys)]
    (= bnf-query bnf-sub-map)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; By Item

(defn sum-items
  "Sum for each item in scrips across all surgeries"
  [scrips]
  (reduce (fn [acc scrip]
            (let [bnf_code (-> scrip :bnf_code :bnf_code)]
              (assoc acc
                bnf_code
                {:count (+ (get-in acc [bnf_code :count] 0)
                           (get scrip :items 0))
                 :bnf_name (:bnf_name scrip)})))
          {}
          scrips))

(defn sum-all
  "Sum all the items in scrips across all types and surgeries"
  [scrips]
  (reduce (fn [acc n] (+ acc (:items n))) 0 scrips))

;; sum all the chemical items that are the same
(defn sum-chemicals [scrips]
  (reduce (fn [acc scrip]
            (let [bnf_code (-> scrip
                               :bnf_code
                               (select-keys [:bnf_chapter :bnf_section :bnf_paragraph :bnf_subparagraph :chemical]))]
              (assoc acc
                bnf_code
                {:count (+ (get-in acc [bnf_code :count] 0)
                           (get scrip :items 0))
                 :bnf_names (conj
                             (get-in acc [bnf_code :bnf_names] #{})
                             (:bnf_name scrip))}))) {} scrips))

(defn- sort-items [items]
  (->> (vec items)
       (sort-by #(-> % second :count))))

(defn topk-items [k items]
  (->> (sort-items items)
       reverse
       (take k)))

(defn bottomk-items [k items]
  (->> (sort-items items)
       (take k)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; By Surgery
;; all the scrips at a surgery
(defn sum-surgeries [scrips]
  (reduce (fn [acc scrip]
            (assoc acc
              (:practice scrip)
              (+ (get acc (:practice scrip) 0)
                 (get scrip :items 0))))
          {}
          scrips))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; By CCG
(defn sum-ccgs [scrips] ;; seq of bnf record maps
  (let [epraccur (ods/epraccur)
        ccgs     (ccg/ccg-names)]
    (->> scrips
         (map #(let [surgery_rec (get epraccur (:practice %))
                     ccg_code    (:parent_organisation_code surgery_rec)]
                 (assoc % :ccg_code ccg_code)))
         (reduce (fn [acc scrip]
                   (assoc acc
                     (:ccg_code scrip)
                     (+ (get acc (:ccg_code scrip) 0)
                        (get scrip :items 0)))) {})
         (map #(let [[ccg_code total_items] %
                     ccg_rec        (get ccgs ccg_code)
                     ccg_population (if ccg_rec
                                      (Integer/parseInt (clojure.string/replace (:registered_population_2012 ccg_rec) "," ""))
                                      nil)]
                 (conj % ccg_population)))
         (remove #(nil? (last %))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; API
(defn prescriptions [rows]
  (let [[header & records] rows
        header-keys (munge/keyword-header header)]
    (map #(pdpi-record header-keys %) records)))

(defn grep-by-bnf
  "Takes a partial bnf map and greps the records that match."
  [bnf-query rows]
  (->> rows
       prescriptions
       (filter #(bnf-match? bnf-query %))))

(defn invert-grep-by-bnf
  "Takes a partial bnf map and greps the records that match."
  [bnf-query rows]
  (->> rows
       prescriptions
       (filter #((complement bnf-match?) bnf-query %))))

(defn pdpi-files
  ([dirname]
     (->> (file-seq (io/file (io/resource dirname)))
          (filter #(.isFile ^java.io.File %))
          sort))
  ([]
     (pdpi-files "gp-prescriptions/pdpi/")))
