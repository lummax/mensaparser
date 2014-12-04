(ns mensaparser.parser
  (:import (java.util Locale)
           (org.joda.time.format DateTimeFormatter))
  (:require
    [clojure.string :as string]
    [net.cgrand.enlive-html :as html]
    [clj-time.format :as tftm]
    [clj-time.core :as ttime]))

(defn with-default-year
  "Return a copy of a formatter that uses the given default year."
  [^DateTimeFormatter f ^Integer default-year]
  (.withDefaultYear f default-year))

(defn get-food-plan-data
  [url]
  (html/select (html/html-resource (java.net.URL. url)) [:div.inside]))

(defn extract-content
  [data selection]
  (string/replace (string/replace (or (first (:content (first (html/select data selection)))) "") #"\n" " ") #"  " " "))

(defn transform-date
  [string]
  (tftm/unparse-local
    (tftm/formatters :year-month-day)
    (tftm/parse-local
      (with-default-year
        (tftm/with-locale
          (tftm/formatter "dd. MMM")
          (Locale. "de"))
        (ttime/year (ttime/now)))
      string)))

(defn date-mapper
  [date-entry]
  [(string/replace (:href (:attrs date-entry)) #"#" "")
    (transform-date (extract-content date-entry [:span.tab-date]))])

(defn generate-date-map
  [food-plan-data]
  (into {} (map date-mapper (html/select food-plan-data [:ul.tabs :li :a]))))

(defn food-mapper
  [food-type-entry]
  [(:data (:attrs food-type-entry))
   (:title (:attrs (first (html/select food-type-entry [:img]))))])

(defn generate-food-map
  [food-plan-data]
  (into {} (map food-mapper (html/select food-plan-data [:ul.food-type-filter :li :a]))))

(defn legend-mapper
  [legend-data]
  (apply hash-map (map (fn
         [element]
         (if
           (string? element)
           (string/replace element #",? *$" "")
           (first (:content element))))
       (rest (:content legend-data)))))

(defn generate-legend-map
  [food-plan-data]
  (into {} (map legend-mapper (html/select food-plan-data [:div.food-plan-legend :div.legend-data]))))

(defn extract-price
  [entry selection]
  (string/replace (string/replace (extract-content entry selection) #"," ".") #"€" ""))

(defn meals-mapper
  [food-map meal-entry]
  {:description  (extract-content meal-entry [:td.field-name-field-description])
   :price-students (extract-price meal-entry [:td.field-name-field-price-students])
   :price-employees (extract-price meal-entry [:td.field-name-field-price-employees])
   :food-types (map #(get food-map %) (re-seq #"food-type-\p{javaLowerCase}+" (:class (:attrs meal-entry))))})

(defn categories-mapper
  [food-map category-table]
  {:name (extract-content category-table [:th.category-name])
   :meals (map #(meals-mapper food-map %) (html/select category-table [:tr.node]))})

(defn food-plan-mapper
  [date-map food-map food-plan]
  {:date (get date-map (:id (:attrs food-plan)))
   :categories (map #(categories-mapper food-map %) (html/select food-plan [:table.food-category]))})

(defn generate-food-plan
  [url]
  (let [food-plan-data (get-food-plan-data url)
        date-map (generate-date-map food-plan-data)
        food-map (generate-food-map food-plan-data)]
    (map #(food-plan-mapper date-map food-map %) (html/select food-plan-data [:div.food-plan]))))
