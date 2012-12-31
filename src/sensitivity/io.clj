(ns sensitivity.io
  (:import java.io.FileInputStream
           java.nio.ByteBuffer
           java.nio.ByteOrder))


(def BUFFER-SIZE 640)

(def MAGIC-HEADER 0x711ad917)

(def OFFSETS [0x1ff800
              0x1ffc00])

(defn open-channel [file]
  (.. (FileInputStream. file)
      getChannel))

(defn channel->bb [channel]
  (let [bb (.order (ByteBuffer/allocate BUFFER-SIZE)
                   ByteOrder/LITTLE_ENDIAN)]
    (.read channel bb)
    (.flip bb)))

(defn read-data-at-offset [channel offset]
  (-> channel
      (.position offset)
      channel->bb
      (decode sensor-data)))

(defn read-data-from-file [file]
  (with-open [chn (open-channel file)]
    (doseq [offset OFFSETS]
      )))

(defcodec sensor-data
  (ordered-map :magic-header :uint32
               :cap-idx :uint32
               :image-idx :uint32
               :img-chksum :uint32
               :img-timestamp :uint32
               :entries (repeated [:uint64
                                   :float32 :float32 :float32
                                   :float32 :float32 :float32 ]
                                  :prefix :byte)
               :padding (repeated :byte :prefix :none)))
