#!/bin/bash

function copy-figures {
dataset="$1"
from="$2"
target="$3"

cp -v "$from/ipc-reports/ipc_score_table.tex" "$target/data/$dataset-ipc-scores.tex"
cp -v "$from/reports/ipc_score_table.tex" "$target/data/$dataset-ipc-scores-short.tex"
cp -v "$from/ipc-reports/quality_line_chart.pdf" "$target/img/$dataset-quality.pdf"
cp -v "$from/plots/gantt_RRAPNSched_p12.pdf" "$target/img/$dataset-gantt-p12-RRAPN.pdf"
cp -v "$from/plots/gantt_TFD2014_p12.pdf" "$target/img/$dataset-gantt-p12-TFD.pdf"
cp -v "$from/plots/gantt_MSFA5Sched_p12.pdf" "$target/img/$dataset-gantt-p12-MSFA5.pdf"
cp -v "$from/ipc-reports/score_line_chart.pdf" "$target/img/$dataset-score.pdf"
cp -v "$from/ipc-reports/runtime_line_chart.pdf" "$target/img/$dataset-runtime.pdf"
}

prefix="$HOME/git"
TEvariant="TransportEditor-final"
TE="TransportEditor"
merged="$prefix/$TEvariant/tools/benchmarks/results/merged"
target="$prefix/$TE/transport-docs/bp"

for track_dat in ipc08seq_seq-sat-6 ipc08temp_tempo-sat-6 ipc11seq_seq-sat-7 ipc14seq_seq-sat-8; do
track="`echo $track_dat | grep -Eo 'ipc[0-9]{2}[a-z]+'`"
dataset="`echo $track_dat | grep -Eo '[a-z]+-[a-z]+-[0-9]'`"
from="$merged/$track"
copy-figures "$dataset" "$from" "$target"
done
