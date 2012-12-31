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
   ByteOrder/BIG_ENDIAN))

(defn read-data-at-offset [channel offset]
  )




(defn read-data-from-file [file]
  (with-open [chn (open-channel file)]
    (doall
     (map (partial read-data-at-offset chn)
          OFFSETS))))

