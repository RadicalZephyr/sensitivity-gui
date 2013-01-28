(ns scanner.acceptance
  (:require clojure.set)
  (:use [scanner.io :only [list-files
                           list-directories]]
        [scanner.sensitivity :only [offset->string
                                    sensitivity->string
                                    validate-root-exists
                                    calculate]]
        [clojure.java.io :only [file]]
        [clojure.string :only [join
                               split]]
        [clojure.java.shell :only [sh]]))


;; So many thoughts!  OK, so since we're going to do a multiple pass
;; comparison, maybe we'll need to write out the output of
;; acceptance.exe to files.  IF SO, put them in target directory,
;; under target/data/<agman-version>/<scan-name>.csv

;; In reality, keeping the data as incanter datasets is probably best
;; to avoid disk I/O and such.  We only need to keep two datasets in
;; memory at once, and just run all the statistical functions over
;; each.

;; Which brings me to: should think of the compare stage as a
;; multi-pass operation.  Somewhere, I should be able to define (or
;; allow the user to) a set of different statistical operations to run
;; over the data and get the results of each one.

;; Also, for the future a DSL for generating expected outputs of agman
;; would be nice.  But we'll punt on that one for now.  For now, the
;; "expected" file will contain a literal line-by-line comparable
;; dataset to the one produced by the scan.  This will be
;; hand-generated...

;; Also, I realize now that the independent program is the better
;; route.  But I need directory structure to take into account that
;; there could be (and will eventually) multiple versions of agman
;; under test.  Also, we may potentially want to be able to compare
;; the metrics to each other over different version of agman.

(defn write-out-config [root-path offsets sensitivities]
  (spit (str root-path "acceptance.cfg")
        (str
         (join "\n"
               [(str "[offset] = "        (offset->string offsets))
                (str "[sensitivity_x] = " (sensitivity->string 0 sensitivities))
                (str "[sensitivity_y] = " (sensitivity->string 1 sensitivities))
                (str "[sensitivity_z] = " (sensitivity->string 2 sensitivities))]))))

(defn dir->test-name
  "Expects a java.io.File as directory"
  [directory]
  (first
   (split
    (.. directory
        getCanonicalFile
        getName)
    #"\.d")))

(defn file->test-name
  "Expects a java.io.File as file"
  [file]
  (.. file
      getCanonicalFile
      getName))

(defn find-test-cases [root-path]
  (let [dir-test-cases (set
                        (map dir->test-name
                             (list-directories root-path)))
        file-test-cases (set
                         (map file->test-name
                              (list-files root-path)))]
    (clojure.set/intersection dir-test-cases
                              file-test-cases)))

(defn get-exe-for-version [version-dir]
  (let [os-name (System/getProperty "os.name")]
    (cond
     (= "Linux" os-name) (file version-dir "acceptance")
     (= "Windows" os-name) (file version-dir "acceptance.exe"))))

(defn find-test-executables
  "Get a list of executables present in the \"bin/\" subdir."
  [root-path]
  (map get-exe-for-version
       (list-directories (str root-path "bin/"))))

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
        {:keys [offsets sensitivities]} (calculate root-dir)
        ;; Identify test-case names
        test-cases (find-test-cases root-path)
        ;; Find the right acceptance executable (or executables) and maintain a version to exe mapping
        agman-executables (find-test-executables root-path)]
    ;; Generate a config for this test
    (write-out-config root-path offsets sensitivities)
    ;; Run the combinations of version X test-case.  These are the "actual" results

    ;; For each "actual", compare with the "expected"

    ;; Produce output, in some format!
    ))