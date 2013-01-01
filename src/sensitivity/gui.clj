(ns sensitivity.gui
  (use [sensitivity.core :only [root-directory->datasets
                                get-offsets
                                get-sensitivities]]
       seesaw.core
       [seesaw.chooser :only [choose-file]]))

(defn bind-choose-file [root selector]
  (let [dir (choose-file
          :selection-mode :dirs-only
          :success-fn (fn [fc file]
                        (str (.getAbsolutePath file) "/")))]
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
                :center "Filler Text!"
                :north (dir-select)))

(defn setup-menu []
  (let [open-scan-action (action :name "Open scan ..."
                                 :tip "Open a scan folder")
        save-datum-action (action :name "Save to datum ..."
                                  :tip "Save current calibration to datum file")
        exit-action (action :name "Exit"
                            :handler (fn [e] (.dispose (to-frame e))))]
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

(defn display [content]
  (config! f :content content))