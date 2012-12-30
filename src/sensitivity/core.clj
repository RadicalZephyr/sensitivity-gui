(ns sensitivity.core
  (:use seesaw.core
        clojure-csv.core))

(defn main-widget []
  "Hello Seesaw")

(defn -main
  "Hello world, seesaw style!"
  [& args]
  (native!)
  (invoke-later
   (-> (frame :title "Calculate Sensitivity")
       (config! :content (main-widget))
       pack!
       show!)))
