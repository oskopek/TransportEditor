#!/bin/bash

set -e

prefix="$HOME/git"
TEvariant="TransportEditor"
TE="TransportEditor"
merged="$prefix/$TEvariant/tools/benchmarks/results/merged"

for track_dat in ipc08seq_seq-sat-6 ipc08temp_tempo-sat-6 ipc11seq_seq-sat-7 ipc14seq_seq-sat-8; do
track="`echo $track_dat | grep -Eo 'ipc[0-9]{2}[a-z]+'`"
python3 scripts/update-best-scores.py "$merged/$track/results-ipc.json" "$prefix/$TEvariant/tools/benchmarks/results-short/merged/$track/results.json"
track2="`echo $track_dat | grep -Eo 'ipc[0-9]{2}'`"
dataset="`echo $track_dat | grep -Eo '[a-z]+-[a-z]+'`"
last_config="`ls $prefix/$TEvariant/tools/benchmarks/configs | grep -E "$dataset" | grep -E "$track2" | grep -v small | tail -n 1`"
. generate-reports.sh "$prefix/$TEvariant/tools/benchmarks/configs/$last_config" "$prefix/$TEvariant/tools/benchmarks/results-short/merged/$track/results.json"
done
