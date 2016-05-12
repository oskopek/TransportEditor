#!/bin/bash

# Solve problems using FastDownward: ./solve.sh input_dataset_number (two digits)

#fd="/home/oskopek/Dropbox/uni/year2/semester2/AP/cvi/fastdownward/fast-downward.py"
fd="fast-downward"
i="$1"

eval "$fd" "--build release64" "../ipc08/seq-opt/transport-strips/p$i.pddl" "--search 'astar(ipdb())'"
cat sas_plan
rm sas_plan output output.sas
