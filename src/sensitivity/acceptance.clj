(ns sensitivity.acceptance
  (:use [clojure.java.shell :only [sh]]
        [sensitivity.core :only [root-directory->datasets
                                 get-means
                                 get-offsets
                                 get-sensitivities
                                 validate-root-exists]]))

(defn write-out-config [root-dir offsets sensitivities]
)

(defn -main
  "Run the acceptance test.  Takes a single argument of a folder.
  This folder should contain the requisite scans for doing a
  sensitivity calibration and any number of identically named
  directory-file pairs of the form \"<file-name>\" and
  \"<file-name>.d\" .

  Each directory should contain an image set for a single scan.  The
  file should contain the expected outputs of the agman when run
  against that scan.

  The file acceptance.cfg will be created the first time the "
  [root-dir]
  (let [root-path     (validate-root-exists root-dir)
        datasets      (root-directory->datasets root-path)
        means         (get-means datasets)
        offsets       (get-offsets means)
        sensitivities (get-sensitivities means)]
    ))