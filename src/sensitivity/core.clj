(ns sensitivity.core
  (:use seesaw.core
        incanter.core)
  (:require incanter.io))

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


(defn- load-file [fname]
  (incanter.io/read-dataset fname :delim \space))

(defn- mean-2d [col]
  (map /
       (reduce #(map + %1 %2) col)
       (repeat (count col))))

(defn- dataset-mean [dataset]
  (let [cols [:col4 :col5 :col6]]
    (to-matrix
     ($ :all cols
        ($rollup mean-2d cols [] dataset)))))

(defn- do-calculation [func datasets]
  (map #(func (:negative %)
              (:positive %)) datasets))

(defn- get-offsets [datasets]
  (do-calculation plus datasets))

(defn iterate-structure [structure func]
  (map #(identity {:negative (func (:negative %))
                   :positive (func (:positive %))})
       structure))