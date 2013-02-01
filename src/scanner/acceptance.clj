(ns scanner.acceptance
  (:require clojure.set)
  (:use [scanner.io :only [list-files
                           list-directories]]
        [scanner.sensitivity :only [offset->string
                                    sensitivity->string
                                    validate-root-exists
                                    calculate
                                    directory->dataset]]
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

;; More thoughts: Use the information generated by agman in a
;; higher-level way.  Look at the trends more abstractly.  Initial
;; thing, back out the avg. velocity (this is dead simple).  ^x/^t =
;; v_avg

;; More generally, the DSL can approach the thing like a physics
;; problem.  If you can specify in the DSL things like acceleration
;; (which we can pretty much always assume is constant, or even
;; better, zero), then a proper description of motion over time can
;; lead to a known (or reasonable) position at arbitrary times in
;; between.  It will contain a lot of assumptions, but it's not too
;; unreasonable (hopefully!!)

;; In essence the DSL needs to be able to say, there was movement in
;; this axis, it went x distance over y seconds.  Then the DSL will
;; produce a function that can be run over the actual dataset and do
;; the comparisons (possibly passed in as function parameters) and
;; return a seq of results.

;; Motion capture idea.  In general, could use an ultrasonic sensor
;; for position (like in physics lab) and a digital encoder for
;; rotation...

(defn write-out-config [root-path offsets sensitivities]
  (let [config-file-path (str root-path "acceptance.cfg")]
    (spit config-file-path
     (str
      (join "\n"
            [(str "[offsets] = "        (offset->string offsets))
             (str "[sensitivity_x] = " (sensitivity->string
                                        0
                                        sensitivities))
             (str "[sensitivity_y] = " (sensitivity->string
                                        1
                                        sensitivities))
             (str "[sensitivity_z] = " (sensitivity->string
                                        2
                                        sensitivities))])))
    config-file-path))

(defn dir->test-name
  "Expects a java.io.File as directory"
  [directory]
  (first
   (split
    (.. directory
        getCanonicalPath)
    #"\.d")))

(defn file->test-name
  "Expects a java.io.File as file"
  [file]
  (.. file
      getCanonicalPath))

(defn find-test-cases
  "Returns a set of string representing all the test-cases found.  Use
  test-name->dataset to get back the actual data from the test"
  [root-path]
  (let [dir-test-cases (set
                        (map dir->test-name
                             (list-directories root-path)))
        file-test-cases (set
                         (map file->test-name
                              (list-files root-path)))]
    (clojure.set/intersection dir-test-cases
                              file-test-cases)))

(defn test-name->dataset [test-name]
  (directory->dataset (str test-name ".d")))

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

(defn run-test-case [exe config-file [expected-fn dataset]]
  (let [temp-file ""]
    (incanter.core/save dataset temp-file
                        :delim " " :header [])
    (sh (.getCanonicalPath exe)
        config-file
        :in temp-file)))

(defn read-from-file [filename]
  (with-open
      [r (java.io.PushbackReader.
          (clojure.java.io/reader filename))]
    (read r)))

(defn get-processing-function [test-case]
  (eval (read-from-file test-case)))

(defn get-version-from-exe [exe]
  (let [path-string (.getCanonicalPath exe)
        path-components (split path-string
                               (re-pattern
                                (java.io.File/separator)))]
    (nth path-components
         (- (count path-components) 2))))

(defn compare-test-case [actual expected])

(defn report-test-case-results [results])

(defn delta-something [units milliseconds device-axis]
  (let [axis-symbol (symbol device-axis)
        velocity (/ units milliseconds)]
    `(fn [~'dataset]
       (let [~'expected-fn (fn [~'timestamp]
                             (* ~velocity ~'timestamp))]
         (for [{:keys [~'timestamp ~axis-symbol]}
               (:rows
                (incanter.core/sel ~'dataset
                                   :cols [:timestamp
                                          ~(keyword device-axis)]))]
           [(~'expected-fn ~'timestamp)
            ~axis-symbol])))))

(defmacro rotated [degrees seconds axis]
  (delta-something degrees (* seconds 1000) (str "gyro-" axis)))

(defmacro translated [inches seconds axis]
  (delta-something inches (* seconds 1000) (str "acc-" axis)))

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
        config-path (write-out-config root-path offsets sensitivities)]
    ;; Generate a config for this test

    ;; Run the combinations of version X test-case.  These are the "actual" results
    ;; Produce output, in some format!
    (report-test-case-results
     (for [;; Iterate over test-datasets
           test-case (map (juxt get-processing-function
                                test-name->dataset)
                          (find-test-cases root-path))
           ;; and test-executables
           exe (find-test-executables root-path)]

       ;; Return a vector pair of ["version" <results>]
       [(get-version-from-exe exe)
        ;; For each "actual", compare with the "expected"
        (run-test-case exe config-path test-case)            ;; Actual
        ]))))    ;; Expected

