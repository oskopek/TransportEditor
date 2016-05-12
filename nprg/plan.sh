#!/bin/bash

# Runs the prolog planner. Takes a standard Transport STRIPS PDDL formatted problem
# and outputs the optimal plan in a standard VAL-compatible format.
# If no output file is specified, prints to stdout.
# If `validate` is on the $PATH, we validate the resulting plan.

# All output from this script (and VAL, if installed) is printed to stderr.
# Only the plan is printed to stdout.

# Parameters: ./plan.sh input_file [output_file]

###################################################################################

inputfile="$1"
outputfile="$2"
tmpout="`mktemp`"
tmpout2="`mktemp`"
domain="inputs/domain.pddl"

echo -e "Planning...\n" 1>&2

./toinput.sh "$inputfile" | swipl 2> /dev/null | tee debug > "$tmpout"
./tooutput.sh "$tmpout" > "$tmpout2"

if [ -n "$outputfile" ]; then
    cp "$tmpout2" "$outputfile"
else
    cat "$tmpout2"
fi

if command -v validate > /dev/null 2>&1; then # if we have validate on the system, run it
    echo 'Validating:' 1>&2
    validate "$domain" "$inputfile" "$tmpout2" 1>&2
else
    echo 'Could not find "validate" executable' 1>&2
fi

rm "$tmpout" "$tmpout2" debug
