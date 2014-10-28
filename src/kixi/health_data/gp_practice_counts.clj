(ns kixi.health-data.gp-practice-counts
  (:require [clojure.data.csv :as csv]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [taoensso.timbre :as log]
            [com.stuartsierra.frequencies :as freq]
            [kixi.health-data.munge :as munge]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Surgery Population Level

(defn gp-population-record [header row]
  (try
    (let [trimmed-row (munge/trim-all row)
          record (zipmap header trimmed-row)]
      (assoc record
        :female_10-14 (Integer/parseInt ^String (:female_10-14 record))
        :male_40-44 (Integer/parseInt ^String (:male_40-44 record))
        :female_90-94 (Integer/parseInt ^String (:female_90-94 record))
        :female_70-74 (Integer/parseInt ^String (:female_70-74 record))
        :male_95+ (Integer/parseInt ^String (:male_95+ record))
        :female_35-39 (Integer/parseInt ^String (:female_35-39 record))
        :male_50-54 (Integer/parseInt ^String (:male_50-54 record))
        :female_30-34 (Integer/parseInt ^String (:female_30-34 record))
        :male_5-9 (Integer/parseInt ^String (:male_5-9 record))
        :male_10-14 (Integer/parseInt ^String (:male_10-14 record))
        :female_80-84 (Integer/parseInt ^String (:female_80-84 record))
        :female_85-89 (Integer/parseInt ^String (:female_85-89 record))
        :female_65-69 (Integer/parseInt ^String (:female_65-69 record))
        :male_45-49 (Integer/parseInt ^String (:male_45-49 record))
        :male_25-29 (Integer/parseInt ^String (:male_25-29 record))
        :female_95+ (Integer/parseInt ^String (:female_95+ record))
        :male_85-89 (Integer/parseInt ^String (:male_85-89 record))
        :female_55-59 (Integer/parseInt ^String (:female_55-59 record))
        :total_all (Integer/parseInt ^String (:total_all record))
        :male_15-19 (Integer/parseInt ^String (:male_15-19 record))
        :male_20-24 (Integer/parseInt ^String (:male_20-24 record))
        :male_60-64 (Integer/parseInt ^String (:male_60-64 record))
        :female_20-24 (Integer/parseInt ^String (:female_20-24 record))
        :female_25-29 (Integer/parseInt ^String (:female_25-29 record))
        :male_30-34 (Integer/parseInt ^String (:male_30-34 record))
        :male_35-39 (Integer/parseInt ^String (:male_35-39 record))
        :male_70-74 (Integer/parseInt ^String (:male_70-74 record))
        :female_75-79 (Integer/parseInt ^String (:female_75-79 record))
        :male_80-84 (Integer/parseInt ^String (:male_80-84 record))
        :male_55-59 (Integer/parseInt ^String (:male_55-59 record))
        :female_40-44 (Integer/parseInt ^String (:female_40-44 record))
        :female_45-49 (Integer/parseInt ^String (:female_45-49 record))
        :female_0-4 (Integer/parseInt ^String (:female_0-4 record))
        :female_15-19 (Integer/parseInt ^String (:female_15-19 record))
        :female_60-64 (Integer/parseInt ^String (:female_60-64 record))
        :male_0-4 (Integer/parseInt ^String (:male_0-4 record))
        :male_90-94 (Integer/parseInt ^String (:male_90-94 record))
        :female_5-9 (Integer/parseInt ^String (:female_5-9 record))
        :male_75-79 (Integer/parseInt ^String (:male_75-79 record))
        :male_65-69 (Integer/parseInt ^String (:male_65-69 record))
        :total_females (Integer/parseInt ^String (:total_females record))
        :total_male (Integer/parseInt ^String (:total_male record))
        :female_50-54 (Integer/parseInt ^String (:female_50-54 record))))
    (catch Throwable t
      (log/errorf t "Could not parse %s" row)
      (throw t))))

;; (gp-practice-counts "gp-prescriptions/gp-practices/GP_Practice_counts.csv")
(defn gp-practice-counts
  ([filename]
     (with-open [rdr (io/reader (io/resource filename))]
       (let [[header-row & records] (csv/read-csv rdr)
             header                 (munge/keyword-header header-row)]
         (->> records
              (map #(gp-population-record header %))
              (map #(vector (:gp_practice_code %) %))
              (into {})
              doall))))
  ([]
     (gp-practice-counts "reference/GP_Practice_counts.csv")")))
