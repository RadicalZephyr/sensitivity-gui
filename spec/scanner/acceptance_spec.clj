(ns scanner.acceptance-spec
  (:use speclj.core
        scanner.io
        scanner.acceptance)
  (:import java.io.File))

(describe "test-name conversions"
          (it "turns directories into test-names"
              (should= "test-name" (dir->test-name
                                    (File. "test-name.d"))))
          (it "turns files into test-names"
              (should= "the-test" (file->test-name
                                   (File. "the-test"))))
          (it "turns mutiple directories into test-names"
              (should= '("my-test" "another" "a-third")
                       (map (comp dir->test-name #(File. %))
                            ["my-test.d" "another.d" "a-third.d"]))))

(describe "find-test-cases"
          (around [it]
                  (with-redefs [list-files (fn [& _]
                                             (map #(File. %)
                                                  ["my-test" "this-test" "notatest"]))
                                list-directories (fn [& _]
                                                   (map #(File. %)
                                                        ["non-testy-dir" "this-test.d" "my-test.d"]))]
                    (it)))

          (let [ test-cases (find-test-cases "non-root")]
            (it "finds the right cases"
                (should= #{"this-test" "my-test"} test-cases)
                (should (and (contains? test-cases "this-test")
                             (contains? test-cases "my-test")))))
          (let [ test-cases (find-test-cases "non-root")]
            (it "doesn't find the not-cases"
                (should-not (or (contains? test-cases "non-testy-dir")
                                (contains? test-cases "notatest"))))))