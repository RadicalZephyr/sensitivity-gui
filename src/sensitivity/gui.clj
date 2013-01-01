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

(defn start-dev []
  (use 'clojure.repl
       'clojure.pprint
       'seesaw.dev))

(defn display [content]
  (config! f :content content))