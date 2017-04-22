#!/bin/bash
configs='echo seq-sat-${ipc}-rrapn seq-sat-${ipc}-sfa3 tempo-sat-${ipc}-tfd2014 tempo-sat-${ipc}-rrapnsched'
TEvariant="TransportEditor-final"
exp_configs=""

for ipc in ipc08 ipc11 ipc14; do
    for config in `eval $configs`; do
        if [ -f "configs/$config.json" ]; then
            echo "Found config: $config"
            exp_configs="$exp_configs $config"
        fi
    done
done


i=0
for config in $exp_configs; do
    echo "cd git/$TEvariant/tools/benchmarks && ./benchmark.sh configs/$config.json; exit" | ssh -tt "u-pl1$i" &
    i=$((i+1))
done

echo "Waiting for jobs to finish..."
wait $(jobs -p)
echo "Ended, waiting for AFS sync"
sleep 30s
cd "$HOME/git/$TEvariant/tools/benchmarks"
. merge-last-results.sh
echo "Merge dir: `pwd`/results/merged"

