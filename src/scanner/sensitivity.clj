(ns scanner.sensitivity
  (:use incanter.core
        [clojure.string  :only [join]]
        [clojure.java.io :only [file]]
        [scanner.io  :only [read-data-from-directory]]))

(def ^:const DEFAULT-STRUCTURE
  [{:neg "Xnegative.d" :pos "Xpositive.d"}
   {:neg "Ynegative.d" :pos "Ypositive.d"}
   {:neg "Znegative.d" :pos "Zpositive.d"}])

(defn- mean-2d
  "Helper function to do a mean over a 2d dataset."
  [col]
  (map /
       (reduce #(map + %1 %2) col)
       (repeat (count col))))

(defn- dataset-mean
  "Collapse a dataset into the mean of the accel data."
  [dataset]
  (let [cols [:acc-x :acc-y :acc-z]]
    ($rollup mean-2d cols [] dataset)))

(defn- num-rows
  "Return the number of rows in a dataset."
  [dataset]
  (count (to-list dataset)))

(defn- do-calculation
  "Apply a function of two args over the dataset structure.  The two
  arguments will be the negative and positive datasets for each axis.
  The result of this function should be a dataset.  Returns that
  result divided by two."
  [f datasets]
  (map #(div (f (to-matrix (:neg %))
                (to-matrix (:pos %)))
             2)
       datasets))

(defn- iterate-structure
  "Walk the structure, applying f to each dataset and using the result
  in it's place."
  [f structure]
  (map #(identity {:neg (f (:neg %))
                   :pos (f (:pos %))})
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
  "Turn a directory into a dataset.  For the arity-2 version, root-dir
  should have a trailing slash."
  ([directory]
     (dataset [:timestamp
               :gyro-x :gyro-y :gyro-z
               :acc-x :acc-y :acc-z
               :mag-x :mag-y :mag-z]
              (read-data-from-directory directory)))
  ([root-dir dir-name]
     (directory->dataset (str root-dir dir-name))))

(defn- root-directory->datasets
  "Create a structure of datasets located at root-path.  The
  single-arity version is a convenience method for using the default
  structure and file-names."
  ([root-path]
     (root-directory->datasets root-path DEFAULT-STRUCTURE))
  ([root-path dir-strc]
     (iterate-structure
      (partial directory->dataset root-path)
      dir-strc)))

(defn get-calibration-filenames []
  (flatten (map #(vals %)
                DEFAULT-STRUCTURE)))

(defn has-calibration-scans?
  "Check whether all of the calibration scan directories exist."
  [root-dir]
  (every? #(.exists %)
          (map (partial file root-dir)
               (get-calibration-filenames))))

(defn offset->string [offsets]
  (join " " (sel (dataset-mean offsets) :rows 0)))

(defn sensitivity->string [row sensitivities]
  (join " " (sel sensitivities :rows row)))

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

(defn root-exists? [root-path]
  (.exists (file root-path)))

(defn canonicalize-path [root-dir]
  (str (.getCanonicalPath
        (file root-dir)) "/"))

(defn normalize-dataset
  "Rebase the timestamps in this dataset to start at 0."
  [ds]
  (let [base-time (sel ds :rows 0 :cols :timestamp)]
    (conj-cols
     ($map (fn [timestamp]
                {:timestamp (- timestamp base-time)})
              :timestamp ds)
     (sel ds :except-cols :timestamp))))

(defn get-calibration
  "From the root-dir, calculate the offsets and sensitivities."
  [root-path]
  (if (and (root-exists?           root-path)
           (has-calibration-scans? root-path))
    (let [datasets (root-directory->datasets root-path)
          means    (iterate-structure dataset-mean
                                      datasets)]
      {:offsets (get-offsets means)
       :sensitivities (get-sensitivities means)})
    nil))

(defn -main
  "Takes a single argument, a folder that has the subfolders Xnegative,
  Xpositive, Ynegative, Ypositive, Znegative, and Zpositive.
  Each subfolder should contain a series of Provel .pbmp files named
  sequentially (i.e. the output of a scan).
  Will print out the offsets and sensitivities of the scanner."
  [root-dir & args]
  (let [root-path (canonicalize-path root-dir)]
    (if-let [{:keys [offsets sensitivities]} (get-calibration root-path)]
      (println
       (config->string offsets sensitivities))
      (println "No sensitivity scans were found."))))
