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

(fact "Windows executables have an .exe extension, linux ones don't"
      (get-exe-for-version "v0.15.1") => (io/file "v0.15.1/acceptance")

      (let [old-os-name (System/setProperty "os.name" "Windows")]
        (get-exe-for-version "v0.15.1") => (io/file "v0.15.1/acceptance.exe")
        (System/setProperty "os.name" old-os-name)))

(fact "string->dataset should return a dataset"
      (string->dataset [:fake] "123")          => ic/dataset?
      (string->dataset [:col0 :col1 :col2] "1,2,3\n4,5,6")
        => (ic/dataset [:col0 :col1 :col2]
                       [[1 2 3]
                        [4 5 6]]))

(fact "Executable paths contain versions as their second-to-last
component"
      (get-version-from-exe
       (io/file "v0.15.1/acceptance")) => "v0.15.1"
       (get-version-from-exe
        (io/file "some/extra/stuff/v0.1.0/nothing")) => "v0.1.0")

(fact "Expectation macros return functions"
      (rotated 0 0 x) => fn?
      (rotated 10 1 x) => fn?
      (translated 0 0 x) => fn?
      (translated 10 1 x) => fn?)

(fact "gen-delta-function returns a function that can iterate over datasets"
      (let [delta-function (rotated 0 0 x)
            ds (ic/dataset [:timestamp :gyro-x]
                           [[1 10]
                            [2 20]
                            [3 30]])]
        (delta-function ds) => (ic/dataset [:timestamp :actual :expected]
                                           [[1 10 0]
                                            [2 20 0]
                                            [3 30 0]])))

(fact "Run test case should return a dataset or nil"
      (run-test-case (io/file "exe") ...cfg... ...dataset...)
        => (ic/dataset [:timestamp
                        :acc-x :acc-y :acc-z
                        :gyro-x :gyro-y :gyro-z]
                       [[0 1 1 1 2 2 2]
                        [1 2 2 2 3 3 3]])
      (provided
       (clojure.java.shell/sh anything anything :in anything)
       => {:out "0,1,1,1,2,2,2\n1,2,2,2,3,3,3"}))