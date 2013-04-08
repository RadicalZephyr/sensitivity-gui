(ns scanner.acceptance-test
  (:require [clojure.java.io :as io]
            [incanter.core :as ic])
  (:use midje.sweet
        scanner.acceptance))

(fact "Transforming a directory to a test-name means getting the last
path component and stripping off '.d' suffix"
      (dir->test-name (io/file "abc.d")) => "abc"
      (dir->test-name (io/file "abc")) => "abc"
      (dir->test-name (io/file "test/abc")) => "abc")

(fact "Transforming a file to a test-name means getting the last path
component"
      (file->test-name (io/file "abc")) => "abc"
      (file->test-name (io/file "test/abc")) => "abc")

(fact "Transforming a data-file to a test-name means getting the last path component and stripping off the 'csv' suffix"
      (data-file->test-name (io/file "abc.csv")) => "abc"
      (data-file->test-name (io/file "abc")) => "abc"
      (data-file->test-name (io/file "test/abc.csv")) => "abc")


(fact "Windows executables have an .exe extension, linux ones don't"
      (get-exe-for-version "v0.15.1") => (io/file "v0.15.1/acceptance")

      (let [old-os-name (System/setProperty "os.name" "Windows")]
        (get-exe-for-version "v0.15.1") => (io/file "v0.15.1/acceptance.exe")
        (System/setProperty "os.name" old-os-name)))

(fact "string->dataset should return a dataset"
      (string->dataset [:fake] "123")          => ic/dataset?
      (string->dataset [:col0 :col1 :col2] "1 2 3\n4 5 6")
        => (ic/dataset [:col0 :col1 :col2]
                       [[1 2 3]
                        [4 5 6]]))

(fact "Executable paths contain versions as their second-to-last
component"
      (get-version-from-exe
       (io/file "v0.15.1/acceptance")) => "v0.15.1"
      (get-version-from-exe
       (io/file "some/extra/stuff/v0.1.0/nothing")) => "v0.1.0")

(fact "Run test case should return a dataset or nil"
      (run-test-case (io/file "exe") ...cfg... ...dataset...)
        => (ic/dataset [:timestamp
                        :gyro-x :gyro-y :gyro-z
                        :acc-x :acc-y :acc-z]
                       [[0 1 1 1 2 2 2]
                        [1 2 2 2 3 3 3]])
      (provided
       (clojure.java.shell/sh anything anything :in anything)
       => {:out "0 1 1 1 2 2 2\n1 2 2 2 3 3 3"}))

(fact "gen-efn should return a function computing d(x) using average
velocity, or zero"
      ((gen-efn ...start-time... ...duration... 0) ...time...) => 0
      ((gen-efn 1 10 10) 1) => 0
      ((gen-efn 1 10 10) 6) => 5
      ((gen-efn 1 10 10) 11) => 10)

(fact "Split dataset should partition a dataset into three parts"
      (split-dataset (ic/dataset [:timestamp :1 :2]
                                 [[0 1 1]
                                  [1 2 2]
                                  [10 11 11]
                                  [34 33 33]
                                  [38 39 39]
                                  [90 99 99]])
                     ;; start-time duration
                     9 26) => [(ic/dataset [:timestamp :1 :2]
                                           [[0 1 1]
                                            [1 2 2]])
                               (ic/dataset [:timestamp :1 :2]
                                           [[10 11 11]
                                            [34 33 33]])
                               (ic/dataset [:timestamp :1 :2]
                                           [[38 39 39]
                                            [90 99 99]])])

(fact "Last-row returns the specified columns in the last row of a dataset"
      (last-row (ic/dataset [:1 :2]
                         [[1 2] [3 4] [5 6]])
                [:1 :2]) => [5 6])

(fact "rms should return the root-mean-squared error of an efn and a dataset"
      (rms (fn [ts] ts)
           [[0 0] [1 1] [2 2]]) => (exactly 0.0)
      (rms (fn [ts] (inc ts))
           [[0 0] [1 1] [2 2]]) => (exactly 1.0))
