#!/bin/bash

# Runs the prolog planner. Takes a standard Transport STRIPS PDDL formatted problem
# and outputs the optimal plan in a standard VAL-compatible format.
# If no output file is specified, prints to stdout.
# If `validate` is on the $PATH, we validate the resulting plan.

# All output from this script (and VAL, if installed) is printed to stderr.
# Only the plan is printed to stdout.

# Parameters: ./plan.sh input_file [output_file]

###################################################################################

function cleanup {
. tooutput.sh "$tmpout" > "$tmpout2"

if [ -n "$outputfile" ]; then
    cp "$tmpout2" "$outputfile"
else
    cat "$tmpout2"
fi

if command -v validate > /dev/null 2>&1; then # if we have validate on the system, run it
    echo -e '\nValidating:' 1>&2
    validate "$domain" "$inputfile" "$tmpout2" 1>&2
fi

rm "$tmpout" "$tmpout2" "$tmpin" debug 2>/dev/null

cd $wd
exit $EXIT_STATUS
}

inputfile=`realpath "$1"`
outputfile=""
if [ -n "$2" ]; then
outputfile=`realpath "$2"`
fi

wd="`pwd`"
dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cd "$dir"

tmpout="`mktemp`"
tmpout2="`mktemp`"
tmpin="`mktemp`"
domain="inputs/domain.pddl"


if [ -z "$inputfile" ]; then
    echo "ERROR: No input file specified. Please supply an argument PDDL file."
    exit
fi

echo -e "Planning...\n" 1>&2

. toinput.sh "$inputfile" > "$tmpin"


trap 'kill -TERM $PID; cleanup' SIGINT SIGTERM EXIT
swipl < "$tmpin" 2>/dev/null 1>"$tmpout" &
PID=$!
wait $PID
trap - SIGINT SIGTERM EXIT
wait $PID
EXIT_STATUS=$?

cleanup
