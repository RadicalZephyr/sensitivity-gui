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

          (let [test-cases (find-test-cases "non-root")]
            (it "finds the right cases"
                (should= #{"this-test" "my-test"} test-cases)
                (should (and (contains? test-cases "this-test")
                             (contains? test-cases "my-test")))))
          (let [ test-cases (find-test-cases "non-root")]
            (it "doesn't find the not-cases"
                (should-not (or (contains? test-cases "non-testy-dir")
                                (contains? test-cases "notatest"))))))