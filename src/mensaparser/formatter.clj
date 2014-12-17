(ns mensaparser.formatter
  (:require
    [clojure.data.xml :as xml]
    [clojure.string :as string]))

(def xml-header
  [:openmensa {:version "2.0"
               :xmlns "http://openmensa.org/open-mensa-v2"
               :xmlns:xsi "http://www.w3.org/2001/XMLSchema-instance"
               :xsi:schemaLocation "http://openmensa.org/open-mensa-v2 http://openmensa.org/open-mensa-v2.xsd"}])

(defn meal-mapper
  [meal]
  [:meal {}
   [:name {} (meal :description)]
   (for
     [note (meal :notes)]
     [:note {} note])
   (if (not= (meal :price-students) "")
     [:price {:role "student"} (meal :price-students)])
   (if (not= (meal :price-employees) "")
     [:price {:role "employee"} (meal :price-employees)])])

(defn category-mapper
  [category]
  (vec (concat
         [:category {:name (category :name)}]
         (mapv meal-mapper (category :meals)))))

(defn day-mapper
  [day]
  (let [categories (mapv category-mapper (filter #(not-empty (% :meals)) (day :categories)))]
    (if (not-empty categories)
      (vec (concat [:day {:date (day :date)}] categories)))))

(defn format-food-plan
  [food-plan]
  (xml/indent-str
    (xml/sexp-as-element
     (vec (concat
            xml-header
            [[:canteen (filter identity (map day-mapper food-plan))]])))))
