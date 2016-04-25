#!/bin/bash

# Parameters: ./plan.sh input_file [output_file]

inputfile="$1"
outputfile="$2"
tmpout="`mktemp`"
tmpout2="`mktemp`"

./toinput.sh "$inputfile" | swipl -s planner.pl -t 'plan(Plan, TotalCost).' -- > "$tmpout"
./tooutput.sh "$tmpout" > "$tmpout2"

if [ -n "$outputfile" ]; then
    cp "$tmpout2" "$outputfile"
else
    cat "$tmpout2"
fi

rm "$tmpout" "$tmpout2"
