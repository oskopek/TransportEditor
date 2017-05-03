#!/bin/bash
set -e

function cleanup {
plan_dir="$1"
old_dir="$2"
cd "$plan_dir"
echo "Writing solution to: $output"
cat $(ls "$solution"* | tail -n 1) > "$output"
rm "$solution"*
rm "output" "output.sas" "all.groups" "variables.groups"
cd "$old_dir"
rm -rf "$plan_dir"
exit "$EXIT_STATUS"
}

function plan {
tfd_dir="$1"
domain="$2"
problem="$3"
solution="$4"
config="y+Y+a+e+r+O+1+C+1+b"

if [ $# != 4 ]; then
    echo "Usage: plan <tfdDir> <domainFile> <problemFile> <solutionFile>"
else
python "$tfd_dir"/translate/translate.py "$domain" "$problem"
"$tfd_dir"/preprocess/preprocess < "output.sas"
"$tfd_dir"/search/search `echo -n $config | tr '+' ' '` "p" "$solution" < "output"
fi

}

old_dir="`pwd`"
plan_dir="`mktemp`"
rm "$plan_dir"
mkdir -p "$plan_dir"
solution="`mktemp`"
solution="`realpath $solution`"
tfd_dir="$HOME/dev/planners/tfd/tfd-src-0.4/downward"
domain="`realpath $1`"
problem="`realpath $2`"
output="`realpath $3`"

EXIT_STATUS="0"

trap 'pkill -TERM -P $PID; cleanup $plan_dir $old_dir' SIGINT SIGTERM
cd "$plan_dir" && plan "$tfd_dir" "$domain" "$problem" "$solution" &
PID=$!
wait $PID
trap - SIGINT SIGTERM
wait $PID
EXIT_STATUS=$?

cleanup

