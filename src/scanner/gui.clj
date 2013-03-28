(ns scanner.gui
  (:gen-class)
  (use [scanner.io :only [save-dataset]]
       [scanner.sensitivity :only [calculate
                                   directory->dataset]]
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
                  (text :id :text-field
                        :editable? false
                        :multi-line? true
                        :text (with-out-str
                                (save-dataset (directory->dataset dir)
                                              "-" :delim ","))))])))

(defn open-sensitivity [root]
  (let [dir (choose-absolute-dir-path root)]
    (reset-main root
                [(scrollable
                  (text :id :text-field
                        :editable? false
                        :multi-line? true
                        :text (with-out-str
                                (scanner.sensitivity/-main dir))))])))

(defn save-file [root]
  (let [file (choose-file root
               :type :save)]
    (invoke-later
     (spit file
           (config (select root [:#text-field])
                   :text)))))

(defn call-with-to-root [fun]
  (fn [e]
    (fun (to-root e))))

(defn setup-menu []
  (let [open-scan-action (action :name "Open scan ..."
                                 :tip "Open a single scan folder"
                                 :handler (call-with-to-root open-scan))
        sensitivity-action (action :name "Open a sensitivity scan ..."
                                   :tip "Open a folder containing the six folders of a sensitivity calibration scan"
                                   :handler (call-with-to-root
                                              open-sensitivity))
        save-action (action :name "Save text area to file ..."
                            :handler (call-with-to-root save-file))
        exit-action (action :name "Exit"
                            :handler (fn [e]
                                       (.dispose (to-frame e))))]
    (menubar :items
             [(menu :text "File" :items [open-scan-action
                                         sensitivity-action
                                         save-action
                                         exit-action])])))

(defn -main
  "Hello world, seesaw style!"
  [& args]
  (native!)
  (invoke-later
   (-> (frame :title "Provel Sensitivity Calibration"
              :on-close :exit
              :menubar (setup-menu)
              :content (main-widget)
              :size [640 :by 480])
       show!)))

(defn start-dev []
  (use 'clojure.repl
       'clojure.pprint
       'seesaw.dev)
  (native!))

(def f)

(defn display [content]
  (config! f :content content))
