#!/usr/bin/env bash
export HOME=/storage/brno2/home/oskopek
module add jdk-8

# TODO: remember to cite MetaCentrum and install val at least

function run-remotely {
TEvaraint="$1"
config="$2"

cd git/$TEvariant/tools/benchmarks
qsub -l select=1:ncpus=16:mem=12gb:scratch_local=25gb -l walltime=2:00:00 "benchmark.sh configs/$config.json"
echo "`date -u '+[%H:%M:%S]'` Finished: $config"
}

#configs='echo seq-sat-${ipc}-rrapn seq-sat-${ipc}-msfa3 seq-sat-${ipc}-msfa5 tempo-sat-${ipc}-tfd2014 tempo-sat-${ipc}-rrapnsched tempo-sat-${ipc}-msfa5sched tempo-sat-${ipc}-trrapn'
configs='echo tempo-sat-${ipc}-rrapnsched tempo-sat-${ipc}-msfa5sched tempo-sat-${ipc}-trrapn'
TEvariant="TransportEditor"

exp_configs=""
for ipc in ipc08 ipc11 ipc14; do
    for config in `eval $configs`; do
        if [ -f "configs/$config.json" ]; then
            echo "Found config: $config"
            exp_configs="$exp_configs $config"
        fi
    done
done

for config in $exp_configs; do
    run-remotely "$TEvariant" "$config"
done

cd "$HOME/git/$TEvariant/tools/benchmarks"
. merge-last-results.sh
echo "Merge dir: `pwd`/results/merged"
