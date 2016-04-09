#!/bin/bash
for i in `ls ipc08/seq-opt/transport-strips/p??.pddl`; do
    echo "Converting $i"
    python3 tests/visualize.py "$i"
done
