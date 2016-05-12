#!/bin/bash

# Parameters: ./tooutput.sh [prolog-planner-output-file]
input="$1"

plan="`cat "$input" | grep -E 'Plan' -m 1`"
planFormatted="`
    echo "$plan" |
    sed -E 's/.*\[(.*)\].*/\1/ ; s/\),/\n/g; s/,$//' |
    tr '()' ',,' |
    sed -E 's/,/ /g; s/truck([0-9]+)/truck-\1/g; s/city([0-9]*)loc([0-9]*)/city\1-loc-\2/g;
    s/package([0-9]*)/package-\1/g; s/ ([0-9]+)/ capacity-\1/g; s/^/(/; s/$/)/; s/pickup/pick-up/g;
    s/[ ]*([)(])[ ]*/\1/g'
`"

cost="`cat "$input" | grep -E 'TotalCost' -m 1`"
costFormatted="`echo "$cost" | grep -oE '[0-9]+'`"

echo "$planFormatted"
echo "; cost = ""$costFormatted"" (general cost)"

