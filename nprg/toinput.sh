#!/bin/bash

# Parameters: ./toinput.sh input_pddl_file

input="$1"
normalized="`cat "$input" | tr -d ':=()-' | tr -s ' '`"

# Swipl
echo "[planner]."
echo "plan(Plan, TotalCost)."

# Roads
echo "roads([""`
    echo "$normalized" |
    grep -E 'roadlength' |
    sed -E 's/roadlength /r(/' |
    sed -E 's/([a-zA-Z0-9]) /\1, /g' |
    sed -E 's/$/),/' |
    tr -d '\n' |
    sed 's/,$//'`""])."

# Packages
packages="`
    echo "$normalized" |
    awk 'BEGIN {goal=0}
    $1 == "goal" {goal=1}
    $1 == "at" && $2 ~ /package/ && goal== 0 {print $0}' |
    sed -E 's/at //' |
    sort
`"
packageDest="`
     echo "$normalized" |
     awk 'BEGIN {goal=0}
     $1 == "goal" {goal=1}
     $1 == "at" && goal == 1 {print $2 " " $3}' |
     sort
`"
tmp="`mktemp`"
echo "$packages" > "$tmp"
respackages="`echo "$packageDest" | join "$tmp" - | sed -E 's/^/p(/;s/$/)/;s/ /, /g' | tr '\n' '+' |
    sed -E 's/\+/, /g' | sed -E 's/, $//'`"
echo "packages([""$respackages""])."

# Vehicles
vehicles="`
    echo "$normalized" |
    grep -E 'at truck' |
    sed -E 's/at //' |
    sed -E 's/^[ ]*//' |
    sed -E 's/(truck[0-9]+)/\1 []/'
`"
capacities="`
    echo "$normalized" |
    grep -E 'capacity ' |
    sed -E 's/^[ ]*capacity[ ]*//' |
    sed -E 's/capacity([0-9]+)/\1/' #|
    #sed -E 's/ ([0-9]+)/ 0 \1/'
`"
echo "$capacities" > "$tmp"
resvehicles="`echo "$vehicles" | join - "$tmp" | sed 's/^/v(/;s/ /, /g;s/$/)/' | tr '\n' '+' | sed -E 's/\+$//;s/\+/, /g' `"
echo "vehicles([""$resvehicles""])."


rm "$tmp"

