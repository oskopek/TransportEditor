#!/bin/bash

# Parameters: ./toinput.sh input_pddl_file [new_input_file]

input="$1"
outputfile="$2"
tmpout="`mktemp`"



if [ -n "$outputfile" ]; then
    cp "$tmpout" "$outputfile"
else
    cat "$tmpout"
fi

rm "$tmpout"
