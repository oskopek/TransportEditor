#!/bin/bash
set -e
rm -rf plans
mkdir plans
for dir in `ls`; do
    if `echo $dir | grep -vE '^[0-9]{2}$' >/dev/null`; then
        continue
    fi

    problemName="p$dir"
    echo "Exporting plan from $problemName"
    for planFile in `ls $dir/ | grep -E 'plan\.soln\.[0-9]+$' | tail -n 1`; do
        file="$dir/$planFile"
        cp $file plans/$problemName-plan.val
    done
done
mv plans tfd-plans
