(ns scanner.acceptance-spec
  (:use speclj.core
        scanner.io
        scanner.acceptance
        [clojure.java.io :only [file]]))

(describe "test-name conversions"
          (it "turns directories into test-names"
              (should= "test-name" (dir->test-name
                                    (file "test-name.d"))))
          (it "turns files into test-names"
              (should= "the-test" (file->test-name
                                   (file "the-test"))))
          (it "turns mutiple directories into test-names"
              (should= '("my-test" "another" "a-third")
                       (map (comp dir->test-name file)
                            ["my-test.d" "another.d" "a-third.d"])))
          (it "turns multiple files into test-names"
              (should= '("my-test" "another" "a-third")
                       (map (comp file->test-name file)
                            ["my-test" "another" "a-third"]))))

(describe "find-test-cases"
          (around [it]
                  (with-redefs [list-files (fn [& _]
                                             (map file
                                                  ["my-test" "this-test" "notatest"]))
                                list-directories (fn [& _]
                                                   (map file
                                                        ["non-testy-dir" "this-test.d" "my-test.d"]))]
                    (it)))

          (it "finds the right cases"
              (let [test-cases (find-test-cases "fake-root")]
                (should (set? test-cases))
                (should= #{"this-test" "my-test"} test-cases)
                (should (and (contains? test-cases "this-test")
                             (contains? test-cases "my-test")))))

          (it "doesn't find the not-cases"
              (let [test-cases (find-test-cases "fake-root")]
                (should-not (or (contains? test-cases "non-testy-dir")
                                (contains? test-cases "notatest"))))))

(describe "Getting version numbers"
          (it "gets the number"
              (should= "13.5"
                       (get-version-from-exe
                        (file "../blah/13.5/acceptance")))))

(describe "Reading code from a file in context"
          (it "returns a function"
              (should (fn?
                       (with-redefs [load-file (fn [& _] (fn []))]
                         (get-processing-function "dummy-file")))))
          (it "throws an exception when the code is bad"
              (with-redefs [load-file (fn [& _] (throw (Exception.)))]
                (should-throw (get-processing-function "dummy-file")))))









