(ns scanner.io
  (:import java.io.RandomAccessFile
           java.nio.ByteBuffer
           java.nio.ByteOrder))

(def BUFFER-SIZE 1024)

(def FW-MAGIC-HEADER 0x711ad917)

(def CALIBRATION-MAGIC-HEADER 0xAC0FF5E7)

(def OFFSETS [0x1ff800
              0x1ffc00])

(defn- open-channel [file]
  (.getChannel (RandomAccessFile. file "r")))

(defn- channel->bb [channel offset]
  (.order
   (.map channel
         java.nio.channels.FileChannel$MapMode/READ_ONLY offset BUFFER-SIZE)
   ByteOrder/LITTLE_ENDIAN))

(defn- read-sensor-entry [byte-buffer]
  (let [timestamp (.getInt byte-buffer)
        accel (repeatedly 3 #(.getShort byte-buffer))
        gyro  (repeatedly 3 #(.getShort byte-buffer))]
    (concat [timestamp] accel gyro)))

(defn- read-data [byte-buffer]
  (let [magic-header (.getInt byte-buffer)
        hdrs (doall (repeatedly 4  #(.getInt byte-buffer)))
        sensor-length (.get byte-buffer)]
    (when (= magic-header FW-MAGIC-HEADER)
      (repeatedly sensor-length #(read-sensor-entry byte-buffer)))))

(defn list-files [directory-name]
  (seq
   (.listFiles (java.io.File. directory-name)
               (reify
                 java.io.FileFilter
                 (accept [this f]
                   (not (.isDirectory f)))))))

(defn list-directories [directory-name]
  (seq
   (.listFiles (java.io.File. directory-name)
               (reify
                 java.io.FileFilter
                 (accept [this f]
                   (.isDirectory f))))))

(defn read-data-from-file [file]
  (with-open [chn (open-channel file)]
    (let [data (map #(read-data (channel->bb chn %))
                    OFFSETS)]
      (when (= (first data) (second data))
        (first data)))))

(defn read-data-from-directory [directory]
  (mapcat read-data-from-file
          (list-files directory)))