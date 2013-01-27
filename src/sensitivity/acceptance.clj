(ns sensitivity.acceptance
  (:use [sensitivity.core :only [offset->string
                                 sensitivity->string
                                 validate-root-exists
                                 root-directory->datasets
                                 get-means
                                 get-offsets
                                 get-sensitivities]]
        [clojure.string :only [join]]
        [clojure.java.shell :only [sh]]))

(defn write-out-config [root-path offsets sensitivities]
  (spit (str root-path "acceptance.cfg")
        (str
         (join "\n"
               [(str "[offset] = "        (offset->string offsets))
                (str "[sensitivity_x] = " (sensitivity->string 0 sensitivities))
                (str "[sensitivity_y] = " (sensitivity->string 1 sensitivities))
                (str "[sensitivity_z] = " (sensitivity->string 2 sensitivities))]))))

(defn -main
  "Run the acceptance test.  Takes a single argument of a folder.
  This folder should contain the requisite scans for doing a
  sensitivity calibration and any number of identically named
  directory-file pairs of the form \"<file-name>\" and
  \"<file-name>.d\" .

  Each directory should contain an image set for a single scan.  The
  file should contain the expected outputs of the agman when run
  against that scan.

  The file acceptance.cfg will be re-created every time this runs."
  [root-dir]
  (let [root-path     (validate-root-exists root-dir)
        datasets      (root-directory->datasets root-path)
        means         (get-means datasets)
        offsets       (get-offsets means)
        sensitivities (get-sensitivities means)]
    (write-out-config root-path offsets sensitivities)
    ))