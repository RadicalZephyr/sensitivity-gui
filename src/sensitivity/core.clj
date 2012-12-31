(ns sensitivity.core
  (:use incanter.core
        sensitivity.io)
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

(defn iterate-structure [func structure]
  (map #(identity {:neg (func (:neg %))
                   :pos (func (:pos %))})
       structure))

(defn list-files [directory]
  (let [f (clojure.java.io/file directory)
        fs (file-seq f)]
    (sort fs)))

(defn do-dirs [dir-strc]
  (iterate-structure
   #(list-files (str "data/1.0/" %))
   dir-strc))

