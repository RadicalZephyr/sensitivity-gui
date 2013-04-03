(ns scanner.gui
  (:gen-class) ;; Necessary so that this can be used as the "main" for
               ;; an uberjar.
  (use seesaw.core
       [seesaw.chooser :only [choose-file]]
       [scanner.io :only [save-dataset]]
       [scanner.sensitivity :only [normalize-dataset
                                   directory->dataset]]))

(defn- choose-absolute-dir-path
  "Open a dialog to choose a directory.  Returns the absolute path
  with a slash appended."
  [root]
  (choose-file root
               :selection-mode :dirs-only
               :success-fn (fn [fc file]
                             (str (.getAbsolutePath file) "/"))))

(defn- main-widget []
  (border-panel :id :main-bp
                :hgap 10 :vgap 10
                :center (vertical-panel  :id :main-display
                                         :maximum-size [640 :by 480]
                                         :items [])))

(defn- reset-main
  "Reset the content of the main text area."
  [root content]
  (invoke-later
   (config! (select root [:#main-display])
            :items content)))

(defn- open-scan [root]
  (let [dir (choose-absolute-dir-path root)]
    (reset-main root
                [(scrollable
                  (text :id :text-field
                        :editable? false
                        :multi-line? true
                        :text (with-out-str
                                (save-dataset
                                 (normalize-dataset
                                  (directory->dataset dir))
                                 "-" :delim ","))))])))

(defn- open-sensitivity [root]
  (let [dir (choose-absolute-dir-path root)]
    (reset-main root
                [(scrollable
                  (text :id :text-field
                        :editable? false
                        :multi-line? true
                        :text (with-out-str
                                (scanner.sensitivity/-main dir))))])))

(defn- save-file [root]
  (let [file (choose-file root
                          :type :save)]
    (invoke-later
     (spit file
           (config (select root [:#text-field])
                   :text)))))

(defn- call-with-to-root
  "Create a wrapper for f that calls to-root on an event and then
  passes the root to f."
  [f]
  (fn [e]
    (f (to-root e))))

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
  "A gui interface to the capabilities of this utility."
  [& args]
  (native!)
  (invoke-later
   (-> (frame :title "Provel Sensitivity Calibration"
              :on-close :exit
              :menubar (setup-menu)
              :content (main-widget)
              :size [640 :by 480])
       show!)))
