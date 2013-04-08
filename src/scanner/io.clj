(ns scanner.io
  (:import java.io.RandomAccessFile
           java.nio.ByteBuffer
           java.nio.ByteOrder))

(def ^:const BUFFER-SIZE 1024)

(def ^:const FW-MAGIC-HEADER
  "The magic number that should be intact in the header of a valid
  frame."
  0x711ad917)

(def ^:const OFFSETS
  "The byte offsets at which the two duplicate sensor readings can be
  found."
  [0x1ff800 0x1ffc00])

(declare read-sensor-entry-v3)

(def VERSIONS
  {:current read-sensor-entry-v3})

(defn- open-channel [filename]
  "Create a channel from a filename."
  (.getChannel (RandomAccessFile. filename "r")))

(defn- channel->bb
  "Memory map a BUFFER-SIZE amount of memory at offset in the given
  channel."
  [channel offset size]
  (.order
   (.map channel
         java.nio.channels.FileChannel$MapMode/READ_ONLY offset size)
   ByteOrder/LITTLE_ENDIAN))

(defn- do-repeatedly [n f]
  (doall
   (repeatedly n f)))

(defn- read-sensor-entry-v1
  [byte-buffer]
  (let [timestamp (.getInt byte-buffer)
        accel (do-repeatedly 3 #(.getFloat byte-buffer))
        gyro  (do-repeatedly 3 #(.getFloat byte-buffer))]
    (concat [timestamp] gyro accel)))

(defn- read-sensor-entry-v2
  [byte-buffer]
  (let [timestamp (.getInt byte-buffer)
        accel (do-repeatedly 3 #(.getShort byte-buffer))
        gyro  (do-repeatedly 3 #(.getShort byte-buffer))]
    (concat [timestamp] gyro accel)))

(defn- read-sensor-entry-v3
  "Read a single sample of sensor data."
  [byte-buffer]
  (let [timestamp (.getInt byte-buffer)
        accel (do-repeatedly 3 #(.getShort byte-buffer))
        gyro  (do-repeatedly 3 #(.getShort byte-buffer))
        mag   (do-repeatedly 3 #(.getShort byte-buffer))]
    ;; Read the Temp and Padding
    (do-repeatedly 2 #(.get byte-buffer))
    (concat [timestamp] gyro accel mag)))

(defn- read-data
  "Read all of the sensor data available from a byte-buffer."
  [byte-buffer read-fn]
  (let [magic-header (.getInt byte-buffer)
        hdrs (do-repeatedly 4  #(.getInt byte-buffer))
        sensor-length (.get byte-buffer)]
    (when (= magic-header FW-MAGIC-HEADER)
      (do-repeatedly sensor-length #(read-fn byte-buffer)))))

(defn list-files
  "Get a sorted sequence of all the files in a directory."
  [directory-name]
  (sort
   (seq
    (.listFiles (java.io.File. directory-name)
                (reify
                  java.io.FileFilter
                  (accept [this f]
                    (not (.isDirectory f))))))))

(defn list-directories
  "Get a sorted sequence of all the sub-directories in a directory."
  [directory-name]
  (sort
   (seq
    (.listFiles (java.io.File. directory-name)
                (reify
                  java.io.FileFilter
                  (accept [this f]
                    (.isDirectory f)))))))

(defn read-data-from-file
  "Read all of the sensor entries from a file.  Verifies that both
  offsets contain the same data.  Returns a seq of sample-seqs
  [timestamp accel_x accel_y accel_z gyro_x gyro_y gyro_z]."
  [read-fn file]
  (with-open [chn (open-channel file)]
    (let [data (map #(read-data read-fn (channel->bb chn % BUFFER-SIZE))
                    OFFSETS)]
      (when (= (first data) (second data))
        (first data)))))

(defn reader-name-for [version]
  (resolve (symbol (str "read-sensor-entry-" (name version)))))

(defn read-data-from-directory
  "Read all of the data for a scan from a directory containing all of
  the PBMP files.  Returns a larger seq of the same form as
  read-data-from-file."
  [directory & {:keys [version] :or {version :current}}]
  (let [reader-fn (or (VERSIONS version)
                      (reader-name-for version))]
    (mapcat (partial read-data-from-file reader-fn)
            (list-files directory))))

;; Lifted and modified from incanter.core (incanter.io)
(defn save-dataset
  "Minor modification of the incanter method for saving a data set to
  allow \"saving\" to std-out by using a \"-\"."
  [dataset filename &
   {:keys [delim header append]
    :or {append false delim \,}}]
  (let [header (or header (map #(if (keyword? %) (name %) %)
                               (:column-names dataset)))
        file-writer (if (= "-" filename)
                      *out*
                      (java.io.FileWriter. filename append))
        rows (:rows dataset)
        columns (:column-names dataset)]
    (do
      (when (and header (not append))
        (.write file-writer (str (first header)))
        (doseq [column-name (rest header)]
          (.write file-writer (str delim column-name)))
        (.write file-writer (str \newline)))
      (doseq [row rows]
        (do
          (.write file-writer (str (row (first columns))))
          (doseq [column-name (rest columns)]
            (.write file-writer (str delim (row column-name))))
          (.write file-writer (str \newline))))
      (.flush file-writer)
      (.close file-writer))))
