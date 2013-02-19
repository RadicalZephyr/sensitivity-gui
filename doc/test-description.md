A test description should look something like this:

    (test-case "test-name"
      (<per-axis specification of movement>)
      ...)
with up to six spec's


where a per-axis spec is:

    (rotation distance duration start-time axis)
    or
    (translation distance duration start-time axis)

Another need is to specify that an axis doesn't move.

Doing this should be simpler than saying (rotated 0 0 0 x).  Maybe it
should be (rotation none x).

Another possibility, I should be able to figure out how far a
translation was, and in what direction/axis from the rotation
specification.  Just calculate it in radians, and then specify the
radius of rotation too.  (later...)
