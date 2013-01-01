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

(defn dir-select []
  (horizontal-panel :items
                    [(text :text "Select a file"
                           :editable? false
                           :id :root-dir)
                     ]))

(defn main-widget []
  (border-panel :id :main-bp
                :hgap 10 :vgap 10
                :center (vertical-panel  :id :main-display
                                         :maximum-size [640 :by 480]
                                         :items [])))

(defn reset-main [root content]
  (invoke-later
   (config! (select root [:#main-display])
            :items content)))

(defn open-scan [root]
  (let [dir (choose-absolute-dir-path root)]
    (reset-main root
                [(scrollable
                  (text :editable? false
                        :multi-line? true
                        :text (with-out-str
                                (prn
                                 (directory->dataset dir)))))])))


(defn open-sensitivity [root]
  (let [dir (choose-absolute-dir-path root)]
    (reset-main root
                [(scrollable
                  (text :editable? false
                        :multi-line? true
                        :text (with-out-str
                                (sensitivity.core/-main dir))))])))

(defn save-datum [root]
  )

(defn call-with-to-root [fun]
  (fn [e]
    (fun (to-root e))))

(defn setup-menu []
  (let [open-scan-action (action :name "Open scan ..."
                                 :tip "Open a single scan folder"
                                 :handler (call-with-to-root open-scan))
        sensitivity-action (action :name "Open a sensitivity scan ..."
                                   :tip "Open a folder containing the six folders of a sensitivity calibration scan"
                                   :handler (call-with-to-root open-sensitivity))
        save-datum-action (action :name "Save to datum ..."
                                  :tip "(NOT IMPLEMENTED) Save current calibration to datum file"
                                  :handler (call-with-to-root save-datum))
        exit-action (action :name "Exit"
                            :handler (fn [e]
                                       (.dispose (to-frame e))))]
    (menubar :items
             [(menu :text "File" :items [open-scan-action
                                         sensitivity-action
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