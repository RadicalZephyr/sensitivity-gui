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

(defn- read-sensor-entry
  "Read a single sample of sensor data."
  [byte-buffer]
  (let [timestamp (.getInt byte-buffer)
        accel (repeatedly 3 #(.getShort byte-buffer))
        gyro  (repeatedly 3 #(.getShort byte-buffer))]
    (concat [timestamp] gyro accel)))

(defn- read-data
  "Read all of the sensor data available from a byte-buffer."
  [byte-buffer]
  (let [magic-header (.getInt byte-buffer)
        hdrs (doall (repeatedly 4  #(.getInt byte-buffer)))
        sensor-length (.get byte-buffer)]
    (when (= magic-header FW-MAGIC-HEADER)
      (repeatedly sensor-length #(read-sensor-entry byte-buffer)))))

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
  [file]
  (with-open [chn (open-channel file)]
    (let [data (map #(read-data (channel->bb chn % BUFFER-SIZE))
                    OFFSETS)]
      (when (= (first data) (second data))
        (first data)))))

(defn read-data-from-directory
  "Read all of the data for a scan from a directory containing all of
  the PBMP files.  Returns a larger seq of the same form as
  read-data-from-file."
  [directory]
  (mapcat read-data-from-file
          (list-files directory)))

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
