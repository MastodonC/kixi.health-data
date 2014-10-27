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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; csv processing
(defn process-csv
  "Takes a function that operates on lazy sequences and a file and
  evaulates the function within a with-open macro. Make sure you call
  doall in the function you pass in to realise the lazy sequence."
  [f file]
  (with-open [rdr (io/reader file)]
    (f (csv/read-csv rdr))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Query Funcion
(defn query-prescriptions-all-months [querier formatter dir]
  (let [files (scrips/pdpi-files dir)]
    (->> (pmap #(vector %1 (process-csv querier %2))
               (map #(.getName %) files)
               files)
         (map formatter)
         (apply concat))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Surgeries
(defn surgery-per-capita
  "Returns ratios of drug items per patient."
  [practice-counts scrips]
  (let [scrip-counts (scrips/sum-surgeries scrips)]
    (->> scrip-counts
         (map #(vector (first %)
                       (munge/per-capita (second %) (-> practice-counts (get (first %)) :total_all))))
         (remove #(nil? (second %))))))

(defn- format-surgeries-months [[name recs]]
  (let [[_ year month] (re-find #"(201.)(..)" name)]
        (mapv (fn [[surgery ratio]] (vector year month surgery (float ratio))) recs)))

(defn top-surgeries [q k rows]
  (let [all-surgeries (counts/gp-practice-counts "gp-prescriptions/gp-practices/GP_Practice_counts.csv")]
    (->> rows
         (scrips/grep-by-bnf q)
         (surgery-per-capita all-surgeries)
         (scrips/topk-surgeries k)
         doall)))

(defn bottom-surgeries [q k rows]
  (let [all-surgeries (counts/gp-practice-counts "gp-prescriptions/gp-practices/GP_Practice_counts.csv")]
    (->> rows
         (scrips/grep-by-bnf q)
         (surgery-per-capita all-surgeries)
         (scrips/bottomk-surgeries k)
         doall)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Chemicals
(defn top-chemicals [q k rows]
  (->> rows
       (scrips/grep-by-bnf q)
       scrips/sum-chemicals
       (scrips/topk-items k)))

(defn- format-items-months [[name recs]]
  (let [[_ year month] (re-find #"(201.)(..)" name)]
    (mapv (fn [[partial-bnf record]]
            (let [{:keys [bnf_chapter bnf_section bnf_paragraph bnf_subparagraph chemical]} partial-bnf
                  {:keys [count bnf_names]} record]
              (vector year
                      month
                      (str bnf_chapter bnf_section bnf_paragraph bnf_subparagraph chemical)
                      count
                      bnf_names)))
          recs)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; API
(defn top-surgeries-all-months [q k dir]
  (query-prescriptions-all-months (partial top-surgeries q k)
                                  format-surgeries-months
                                  dir))

(defn bottom-surgeries-all-months [q k dir]
  (query-prescriptions-all-months (partial bottom-surgeries q k)
                                  format-surgeries-months
                                  dir))

(defn top-chemicals-all-months [q k dir]
  (query-prescriptions-all-months (partial top-chemicals q k)
                                  format-items-months
                                  dir))
