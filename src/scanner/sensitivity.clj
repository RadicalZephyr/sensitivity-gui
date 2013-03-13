(ns scanner.sensitivity
  (:use incanter.core
        [clojure.string  :only [join]]
        [clojure.java.io :only [file]]
        [scanner.io  :only [read-data-from-directory]]))

(def structure [{:neg "Xnegative.d" :pos "Xpositive.d"}
                {:neg "Ynegative.d" :pos "Ypositive.d"}
                {:neg "Znegative.d" :pos "Zpositive.d"}])

(defn- mean-2d [col]
  (map /
       (reduce #(map + %1 %2) col)
       (repeat (count col))))

(defn- dataset-mean [dataset]
  (let [cols [:acc-x :acc-y :acc-z]]
    ($rollup mean-2d cols [] dataset)))

(defn- num-rows [dataset]
  (count (to-list dataset)))

(defn- do-calculation [func datasets]
  (map #(div (func (to-matrix (:neg %))
                   (to-matrix (:pos %)))
             2)
       datasets))

(defn- iterate-structure [func structure]
  (map #(identity {:neg (func (:neg %))
                   :pos (func (:pos %))})
       structure))

(defn- get-offsets [mean-datasets]
  (dataset [:acc-x :acc-y :acc-z]
           (do-calculation plus mean-datasets)))

(defn- get-sensitivities [mean-datasets]
  (matrix
   (do-calculation minus mean-datasets)))

(defn- get-means [datasets]
  (iterate-structure dataset-mean datasets))

(defn directory->dataset
  ([root-dir filename]
     (directory->dataset (str root-dir filename)))
  ([directory]
     (dataset [:timestamp :gyro-x :gyro-y :gyro-z :acc-x :acc-y :acc-z]
              (read-data-from-directory directory))))

(defn- root-directory->datasets
  ([root-path]
     (root-directory->datasets root-path structure))
  ([root-path dir-strc]
     (iterate-structure
      (partial directory->dataset root-path)
      dir-strc)))

(defn validate-root-exists [root-dir]
  (let [root-file (file root-dir)]
    (when (not (.exists root-file))
      (binding [*out* *err*]
        (prn "Error: " root-file " does not exist."))
      (System/exit 1))
    (str (.getCanonicalPath root-file) "/")))

(defn offset->string [offsets]
  (join " " (sel (dataset-mean offsets) :rows 0)))

(defn sensitivity->string [row sensitivities]
  (join " " (sel sensitivities :rows row)))

(defn calculate [root-dir]
  (let [root-path (validate-root-exists root-dir)
        datasets (root-directory->datasets root-path)
        means    (iterate-structure dataset-mean
                                    datasets)]
    {:offsets (get-offsets means)
     :sensitivities (get-sensitivities means)}))

(defn config->string [offsets sensitivities]
  (str
   (join "\n"
         [(str "[offsets] = "        (offset->string offsets))
          (str "[sensitivity_x] = " (sensitivity->string
                                     0
                                     sensitivities))
          (str "[sensitivity_y] = " (sensitivity->string
                                     1
                                     sensitivities))
          (str "[sensitivity_z] = " (sensitivity->string
                                     2
                                     sensitivities))])))

(defn -main
  "Takes a single argument, a folder that has the subfolders Xnegative,
  Xpositive, Ynegative, Ypositive, Znegative, and Zpositive.  Each subfolder
  should contain a series of Provel .pbmp files named sequentially (i.e. the
  output of a scan).  Will print out the offsets and sensitivities of the
  scanner."
  [root-dir & args]

  (let [{:keys [offsets sensitivities]} (calculate root-dir)]
    (println (config->string offsets sensitivities))))
