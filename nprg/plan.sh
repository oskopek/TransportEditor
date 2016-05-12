#!/bin/bash

# Parameters: ./plan.sh input_file [output_file]

inputfile="$1"
outputfile="$2"
tmpout="`mktemp`"
tmpout2="`mktemp`"

#echo -e "Planning...\n"

./toinput.sh "$inputfile" | swipl 2> /dev/null | tee debug > "$tmpout"
./tooutput.sh "$tmpout" > "$tmpout2"

if [ -n "$outputfile" ]; then
    cp "$tmpout2" "$outputfile"
else
    cat "$tmpout2"
fi

rm "$tmpout" "$tmpout2" debug
