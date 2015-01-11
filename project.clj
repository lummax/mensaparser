(defproject mensaparser "0.1.0-SNAPSHOT"
  :description "A 'Studentenwerk Bremen' to openmensa.org converter"
  :url "https://github.com/lummax/mensaparser"
  :license {:name "MIT License"
            :url "http://www.opensource.org/licenses/mit-license.php"
            :distribution :repo}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.xml "0.0.8"]
                 [enlive "1.1.5"]
                 [clj-time "0.9.0"]
                 [ring/ring-jetty-adapter "1.2.2"]
                 [compojure "1.2.2"]
                 [environ "0.5.0"]]
  :main ^:skip-aot mensaparser.core
  :target-path "target/%s"
  :uberjar-name "clojure-mensaparser-standalone.jar"
  :profiles {:production {:env {:production true}}})
