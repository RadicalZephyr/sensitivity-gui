(defproject scanner "0.5.0-SNAPSHOT"
  :description "A library/gui for calculating accelerometer sensitivies"
  :url "https://github.com/ezephyr/sensitivity-gui"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main scanner.gui
  :launch4j-config-file "resources/config.xml"
  :plugins [[lein-launch4j "0.1.1"]
            [lein-midje "3.0.0"]]
  :profiles {:dev {:dependencies [[midje "1.5.1"]]}}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [incanter/incanter-core "1.4.1"]
                 [incanter/incanter-io "1.4.1"]
                 [incanter/incanter-charts "1.4.1"]
                 [seesaw "1.4.3"]])
