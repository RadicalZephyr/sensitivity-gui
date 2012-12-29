(ns sensitivity.core
  (:use seesaw.core))

(defn main-widget []
  "Hello Seesaw")

(defn -main
  "Hello world, seesaw style!"
  [& args]
  (native!)
  (invoke-later
   (-> (frame :title "Synapse Administration")
       (config! :content (main-widget))
       pack!
       show!)))
