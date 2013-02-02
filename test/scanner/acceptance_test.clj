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
      (string->dataset "123")          => ic/dataset?
      (string->dataset "1,2,3\n4,5,6") => (ic/dataset [:col0 :col1 :col2]
                                                      [[1 2 3] [4 5 6]]))
(fact "executable paths contain versions as their second-to-last
component"
      (get-version-from-exe
       (io/file "v0.15.1/acceptance")) => "v0.15.1"
       (get-version-from-exe
        (io/file "some/extra/stuff/v0.1.0/nothing")) => "v0.1.0")

