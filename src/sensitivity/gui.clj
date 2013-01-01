(ns sensitivity.gui
  (use [sensitivity.core :only [directory->dataset
                                root-directory->datasets
                                get-offsets
                                get-sensitivities]]
       seesaw.core
       [seesaw.chooser :only [choose-file]]))

(defn choose-absolute-dir-path [root]
  (choose-file root
               :selection-mode :dirs-only
               :success-fn (fn [fc file]
                             (str (.getAbsolutePath file) "/"))))

(defn bind-choose-file [root selector]
  (let [dir (choose-absolute-dir-path root)]
    (invoke-later
     (config! (select root selector)
              :text dir))))

(defn dir-select []
  (horizontal-panel :items
                    [(text :text "Select a file"
                           :editable? false
                           :id :root-dir)
                     (action :name "..."
                             :handler (fn [e]
                                        (bind-choose-file
                                         (to-root e) [:#root-dir])))]))

(defn main-widget []
  (border-panel :hgap 10 :vgap 10
                :center (flow-panel  :id :main-display
                                     :items ["Filler text"])
                :north (dir-select)))

(defn open-scan [event]
  (let [root (to-root event)
        dir (choose-absolute-dir-path root)]
    (invoke-later
     (config! (select root [:#main-display])
              :items [(scrollable
                       (text :editable? false
                             :multi-line? true
                             :text (with-out-str
                                     (prn (directory->dataset dir)))))]))))

(defn save-datum [event]
  )

(defn setup-menu []
  (let [open-scan-action (action :name "Open scan ..."
                                 :tip "Open a single scan folder"
                                 :handler open-scan)
        save-datum-action (action :name "Save to datum ..."
                                  :tip "Save current calibration to datum file"
                                  :handler save-datum)
        exit-action (action :name "Exit"
                            :handler (fn [e]
                                       (.dispose (to-frame e))))]
    (menubar :items
             [(menu :text "File" :items [open-scan-action
                                         save-datum-action
                                         exit-action])])))

(defn -main
  "Hello world, seesaw style!"
  [& args]
  (native!)
  (invoke-later
   (-> (frame :title "Provel Sensitivity Calibration"
              :menu (setup-menu)
              :content (main-widget)
              :size [400 :by 240])
       (config! )
       pack!
       show!)))

(defn start-dev []
  (use 'clojure.repl
       'clojure.pprint
       'seesaw.dev)
  (native!))

(def f)

(defn display [content]
  (config! f :content content))