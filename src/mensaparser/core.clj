(ns mensaparser.core
  (:require
    [clojure.pprint :as pp]
    [mensaparser.parser :as parser]
    [mensaparser.formatter :as formatter]
    [compojure.core :refer [defroutes GET]]
    [compojure.handler :refer [site]]
    [compojure.route :as route]
    [ring.adapter.jetty :as jetty]
    [environ.core :refer [env]]))

(def urls {"uni-mensa" "http://www.stw-bremen.de/de/essen-trinken/uni-mensa"
           "uni-gw2" "http://www.stw-bremen.de/de/essen-trinken/cafeteria-gw2"
           "uni-nw1" "http://www.stw-bremen.de/de/essen-trinken/mensa-nw-1"
           "neustadtwall" "http://www.stw-bremen.de/de/essen-trinken/mensa-neustadtswall"
           "werderstraße" "http://www.stw-bremen.de/de/essen-trinken/mensa-der-werderstra%C3%9Fe"
           "airport" "http://www.stw-bremen.de/de/essen-trinken/mensa-am-airport"
           "mensa-bremerhaven" "http://www.stw-bremen.de/de/essen-trinken/mensa-bremerhaven"})

(defn process-url
  [url]
  (formatter/format-food-plan (parser/generate-food-plan url)))

(defroutes app
  (GET "/mensa/:id" [id]
       (if (contains? urls id)
         (process-url (urls id))
         (route/not-found "<h1>Invalid</h1>")))
  (route/not-found "<h1>Invalid</h1>"))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (site #'app) {:port port :join? false})))
