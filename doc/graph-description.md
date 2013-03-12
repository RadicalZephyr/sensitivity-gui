Okay, I have the output in the raw format I wanted.  Now I need it an
entirely different one.

Right now, I have this:

    (["10degXrad19pt5"
      {:x-rotation
       {:pre-test
        {:RMS-error
         [["v0.16.0" 0.021724638980291477]
          ["v0.17.0" 5.260855741138949]
          ["v0.18.0" 5.260855741138949]
          ["v0.18.1" 3.2398152565321943]
          ["v0.19.0" 6.94920165752752]
          ["v0.20.0" 6.94920165752752]
          ["v0.20.1" 6.951281782724553]],
         :expected ...
         :actual ...},
        :test ...
        :post-test ...}
       :y-rotation ...}]
      ["other test"
       ...]
      ...)

What I want is this ultimately:

    "versions" "t1RMS" "t1ABS" "t2RMS" "t2ABS" "t3RMS" "t3ABS"
    "v0.16.0"   0.0217  0.0217  0.0217  0.0217  0.0217  0.0217
    "v0.17.0"   5.2608  5.2608  5.2608  5.2608  5.2608  5.2608
    "v0.18.0"   5.2608  5.2608  5.2608  5.2608  5.2608  5.2608
    "v0.18.1"   3.2398  3.2398  3.2398  3.2398  3.2398  3.2398

And equivalently, the transpose of above:

    "tests" "version1-RMS" "version1-ABS" "version2-RMS" "version2-ABS"
    "test1"   0.0217     0.0217    5.2608     5.2608
    "test2"   0.0217     0.0217    5.2608     5.2608
    "test3"   0.0217     0.0217    5.2608     5.2608

Each of these datasets should be associated with a hierarchy of
classifiers in this order:

    device/axis/portion/metric

where portion refers to pre, post, or test and metric refers to RMS or
absolute error (actual - expected).
