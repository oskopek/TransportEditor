#!/bin/bash

# Parameters: ./plan.sh input_file [output_file]

inputfile="$1"
outputfile="$2"
tmpout="`mktemp`"

./to_input.sh "$inputfile" | swipl -s planner.pl -t 'plan(Plan, TotalCost).' > "$tmpout"

if [ -n "$outputfile" ]; then
    cp "$tmpout" "$outputfile"
else
    cat "$tmpout"
fi

rm "$tmpout"
