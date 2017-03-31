#!/bin/bash

function plan {
tfd_dir="$1"
domain="$2"
problem="$3"
output="$4"
config="y+Y+a+e+r+O+1+C+1+b"

if [ $# != 4 ] ; then
    echo "Usage: plan <tfdDir> <domainFile> <problemFile> <solutionFile>"
else
python "$tfd_dir"/translate/translate.py "$domain" "$problem"
"$tfd_dir"/preprocess/preprocess < "output.sas"
exec "$tfd_dir"/search/search `echo -n $config | tr '+' ' '` "p" "$output" < "output"
fi
}

solution="`mktemp`"
solution="`realpath $solution`"
tfd_dir="$HOME/dev/planners/tfd/tfd-src-0.4/downward"
domain="`realpath $1`"
problem="`realpath $2`"

function cleanup {
cat $(ls "$solution"* | tail -n 1)
rm "$solution"*
rm "output" "output.sas"
exit "$EXIT_STATUS"
}

EXIT_STATUS="1"

cd

trap 'pkill -TERM -P $PID; cleanup' SIGINT SIGTERM
plan "$tfd_dir" "$domain" "$problem" "$solution" 1>&2 &
PID=$!
wait $PID
trap - SIGINT SIGTERM
wait $PID
EXIT_STATUS=$?

cleanup

