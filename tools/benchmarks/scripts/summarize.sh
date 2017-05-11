#!/bin/bash
root="results-long/merged"
files=""
for dir in `ls $root | grep seq`; do
    files="$files `realpath $root/$dir/results.json`"
done
echo "Sequential"
echo "=========="
python3 scripts/summarize.py $files

files=""
for dir in `ls $root | grep -v seq`; do
    files="$files `realpath $root/$dir/results.json`"
done
echo
echo
echo
echo "Temporal"
echo "========"
python3 scripts/summarize.py $files

