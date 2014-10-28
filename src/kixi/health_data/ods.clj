(ns kixi.health-data.ods
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [taoensso.timbre :as log]))

(def epraccur-header [:organisation_code :name :national_grouping :high_level_health :address_line_1 :address_line_2 :address_line_3 :address_line_4 :address_line_5 :postcode :open_date :close_date :status_code :organisation_sub-type_code :parent_organisation_code :join_parent_date_8_no :left_parent_date_8_no :contact_telephone_number :null_1 :null_2 :null_3 :amended_record_indicator :null_4 :null_5 :null_6 :practice_type :null_6])

(defn slugify-address [rec]
  (->> rec
       ((juxt :address_line_1 :address_line_2 :address_line_3 :address_line_4 :address_line_5 :postcode))
       (filter seq)
       (clojure.string/join ", ")))

(defn epraccur
  ([filename]
     (with-open [rdr (io/reader (io/resource filename))]
       (->> (csv/read-csv rdr)
            (map #(zipmap epraccur-header %))
            (map #(assoc % :full_address (slugify-address %)))
            (map #(vector (:organisation_code %) %))
            (into {}))))
  ([]
     (epraccur "reference/epraccur.csv")))
