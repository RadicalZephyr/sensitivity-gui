(ns sensitivity.io
  (:import java.io.RandomAccessFile
           java.nio.ByteBuffer
           java.nio.ByteOrder))


(def BUFFER-SIZE 640)

(def MAGIC-HEADER 0x711ad917)

(def OFFSETS [0x1ff800
              0x1ffc00])

(defn open-channel [file]
  (.getChannel (RandomAccessFile. file "r")))

(defn channel->bb [channel offset]
  (.order
   (.map channel
         java.nio.channels.FileChannel$MapMode/READ_ONLY offset BUFFER-SIZE)
   ByteOrder/LITTLE_ENDIAN))

(defn read-sensor-entry [byte-buffer]
  (let [timestamp (.getInt byte-buffer)
        accel (apply vector (repeatedly 3 #(.getFloat byte-buffer)))
        gyro  (apply vector (repeatedly 3 #(.getFloat byte-buffer)))]
    [timestamp accel gyro]))

(defn read-data [byte-buffer]
  (let [magic-header (.getInt byte-buffer)
        hdrs (doall (repeatedly 4  #(.getInt byte-buffer)))
        sensor-length (.get byte-buffer)]
    (repeatedly sensor-length #(read-sensor-entry byte-buffer))))




(defn read-data-from-file [file]
  (with-open [chn (open-channel file)]
    (doall
     (map (partial read-data-at-offset chn)
          OFFSETS))))

