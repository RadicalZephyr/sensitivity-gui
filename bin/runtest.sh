#!/bin/bash

TEMP=$(getopt -o '' -n $(basename $0) -- "$@")

if [ $? != 0 ] ; then echo "Terminating..." >&2 ; exit 1 ; fi

eval set -- "$TEMP"

while true; do
  case "$1" in
    -- ) shift; break ;;
  esac
done

if [ $# -ne 1 ]
then
    echo "Must specify the test directory" && exit 1
fi

echo "Setting up..."
TESTDIR="$1"
TESTNAME=$(basename $TESTDIR)
TESTOUTPUT="target/$TESTNAME.csv"
TESTRES="target/$TESTNAME-results.txt"

echo "Refreshing lein deps"
lein deps

echo "Running test..."
lein run -m scanner.acceptance $TESTDIR > $TESTOUTPUT

echo "Done, analyzing output"
bin/analyze.r $TESTOUTPUT 2> /dev/null > $TESTRES

cat $TESTRES
