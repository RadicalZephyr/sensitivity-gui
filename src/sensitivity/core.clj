(ns sensitivity.core
  (:use incanter.core
        [sensitivity.io :only [read-data-from-file]])
  (:require incanter.io
            clojure.java.io))

(defn main-widget []
  "Hello Seesaw")

;; (defn -main
;;   "Hello world, seesaw style!"
;;   [& args]
;;   (native!)
;;   (invoke-later
;;    (-> (frame :title "Calculate Sensitivity")
;;        (config! :content (main-widget))
;;        pack!
;;        show!)))

(defn- mean-2d [col]
  (map /
       (reduce #(map + %1 %2) col)
       (repeat (count col))))

(defn- dataset-mean [dataset]
  (let [cols [:acc-x :acc-y :acc-z]]
    ($rollup mean-2d cols [] dataset)))

(defn- do-calculation [func datasets]
  (map #(func (:negative %)
              (:positive %)) datasets))

(defn- get-offsets [datasets]
  (do-calculation plus datasets))

(defn iterate-structure [func structure]
  (map #(identity {:neg (func (:neg %))
                   :pos (func (:pos %))})
       structure))

(defn list-files [directory]
  (let [f (clojure.java.io/file directory)
        fs (file-seq f)]
    (drop 1 (sort fs))))

(defn directory->dataset [root-dir filename]
  (dataset [:timestamp :acc-x :acc-y :acc-z :gyro-x :gyro-y :gyro-z]
           (mapcat read-data-from-file
                   (list-files (str root-dir filename)))))

(defn do-dirs [root-dir dir-strc]
  (iterate-structure
   (partial directory->dataset root-dir)
   dir-strc))

