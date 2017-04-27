#!/usr/bin/env bash

function merge-results {
prefix="$1"
configs="$2"
target="$3"

results=""
lastconfig=""
for config in $configs; do
    lastconfig="$config"
    confdir="$prefix/results/$config"
    if ! [ -d "$confdir" ]; then
        echo "Conf dir not found, skipping: $confdir"
        continue
    fi
    lastres="`ls $confdir | tail -n 1`"
    if [ -z "$lastres" ]; then
        echo "Last Conf dir not found, skipping: $lastres"
        continue
    fi
    file="`realpath $confdir/$lastres/results.json`"
    if [ -f "$file" ]; then
        results="$results $file"
    else
        echo "Skipping $config, not results file found: $file"
        continue
    fi
done

if [ -z "$results" ]; then
    echo "No result dirs found for configs: $configs"
    return
fi

rm -rf "$target"
sleep 1s
mkdir -p "$target"
python3 scripts/merge-results.py $results "$target/results.json"
. generate-reports.sh "$lastconfig" "$target/results.json"

}

ipc08seq="seq-sat-ipc08-rrapn seq-sat-ipc08-msfa3 seq-sat-ipc08-msfa5"
ipc11seq="seq-sat-ipc11-rrapn seq-sat-ipc11-msfa3 seq-sat-ipc11-msfa5"
ipc14seq="seq-sat-ipc14-rrapn seq-sat-ipc14-msfa3 seq-sat-ipc14-msfa5"
ipc08temp="tempo-sat-ipc08-rrapnsched tempo-sat-ipc08-tfd2014 tempo-sat-ipc08-msfa3sched tempo-sat-ipc08-msfa5sched"

#TEvariant="TransportEditor-final"
#basepath="$HOME/git/$TEvariant/tools/benchmarks"
basepath="."
merged="$basepath/results/merged"
merge-results "$basepath" "$ipc08seq" "$merged/ipc08seq"
merge-results "$basepath" "$ipc11seq" "$merged/ipc11seq"
merge-results "$basepath" "$ipc14seq" "$merged/ipc14seq"
merge-results "$basepath" "$ipc08temp" "$merged/ipc08temp"
