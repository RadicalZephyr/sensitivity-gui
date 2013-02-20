A test description should look something like this:

    (deftest "test-name"
      start-time duration
      (<per-axis specification of movement>)
      ...)
with up to six spec's


where a per-axis spec is:

    (rotation axis distance)
    or
    (translation axis distance)

Another need is to specify that an axis doesn't move.

Doing this should be simpler than saying (rotated 0 0 0 x).  Maybe it
should be (rotation none x).

Another possibility, I should be able to figure out how far a
translation was, and in what direction/axis from the rotation
specification.  Just calculate it in radians, and then specify the
radius of rotation too.  (later...)


Break apart the problem into what stays the same, no matter what the
specifics of the test are, and what differs.  The differences need to
be macros (or parameterized functions), and the same parts can be
functions.

Parts:
- normalizing the dataset
- splitting the dataset into three parts (pre, test, post)
- checking the data set against the expected-fn (this needs to happen
  for every section)
- formatting the data structure of the results


Reformatting the data structure
---------------------------

What I want to do, is take the nested hierarchy:

    ["test-name"
     "version"
     {:axis-behaviour {:pre-test {:rms value
                                  :expected value
                                  :actual value}}}]


==>

    ["test-name"
     {:axis-behaviour {:pre-test {:rms ["version" value]
                                  :expected ["version" value]
                                  :actual ["version" value]}}}]

==>

    ["test-name"
     {:axis-behaviour {:pre-test {:rms [["version" value]
                                        ["version" value]...]
                                  :expected [["version" value]
                                             ["version" value]...]
                                  :actual [["version" value]
                                           ["version" value]...]}


First off, I have an extra seq in there somewhere, probably from a for
loop that's unnecessary.

