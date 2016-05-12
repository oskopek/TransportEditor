#!/bin/bash

# Solve Transport STRIPS problems using FastDownward.
# Configure your fast-downward executable here:

#fd="/home/oskopek/Dropbox/uni/year2/semester2/AP/cvi/fastdownward/fast-downward.py --build release64"
fd="fast-downward --build release64"

# For validation, please compile the VAL plan validator (Google it)
# and put the `validate` executable on your $PATH.

# Parameters: ./solve.sh [input_pddl]

######################################################################

input="$1"

eval "$fd" "inputs/domain.pddl" "$input" "--search 'astar(ipdb())'"
cat sas_plan
rm sas_plan output output.sas
