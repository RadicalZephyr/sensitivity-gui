(ns sensitivity.gui
  (use [sensitivity.core :only [root-directory->datasets
                                get-offsets
                                get-sensitivities]]
       seesaw.core
       [seesaw.chooser :only [choose-file]]))

(defn bind-choose-file [selector]
  (choose-file
   :selection-mode :dirs-only
   :success-fn (fn [fc file]
                 (config! (select selector)
                          :text (str (.getAbsolutePath file) "/")))))

(defn dir-select []
  (horizontal-panel :items
                    [(text :text "Select a file"
                           :editable? false
                           :id :root-dir)
                     (action :name "..."
                             :handler #(bind-choose-file [:root-dir]))]))

(defn main-widget []
  "Hello Seesaw")

(defn -main
  "Hello world, seesaw style!"
  [& args]
  (native!)
  (invoke-later
   (-> (frame :title "Calculate Sensitivity"
              :content (main-widget)
              :size [400 :by 240])
       (config! )
       pack!
       show!)))
