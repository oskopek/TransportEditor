#!/bin/bash

function convertToPdf {
if `which rsvg-convert >/dev/null`; then # TODO: PDF/1-A compliance!!!
    for svg in `ls $reportdir/*.svg`; do
        filename="$reportdir"/"`basename $svg | sed 's/\.svg//g'`"
        rsvg-convert -f pdf -o "$filename".pdf "$filename".svg
    done
fi
}

if [ -z "$transportroot" ]; then
transportroot="`realpath "../../"`"
fi
if [ -z "$reportGenerator" ]; then
benchmarker="$transportroot/transport-report/target/""`ls "$transportroot/transport-report/target/" | grep 'transport-report-.*-jar-with-dependencies.jar' | tail -n 1`"
fi

set -e
if [ -z "$2" ]; then
    echo "Please supply a benchmark config and result file as the first and second argument." 1>&2
    exit
fi
config="`realpath "$1"`"
configName="`basename $config | sed 's/\.json//'`"
results="`realpath "$2"`"
resultdir="`echo $results | sed -E 's@/[^/]*$@@'`"
logdir="$resultdir"

echo "`date -u '+[%H:%M:%S]'` Generating reports..."
eval "$JAVA_HOME/bin/java -Dlogging.path=\"$logdir\" -Dtransport.root=\"$transportroot\" -jar $reportGenerator $resultdir/results.json reports"
echo "`date -u '+[%H:%M:%S]'` Generating reports done."

echo "`date -u '+[%H:%M:%S]'` Converting plots..."
reportdir="$resultdir"/reports
convertToPdf
echo "`date -u '+[%H:%M:%S]'` Plots converted."

echo "`date -u '+[%H:%M:%S]'` Adding IPC results..."

if `echo $configName | grep 'ipc08' > /dev/null`; then
ipc="08"
fi
if `echo $configName | grep 'ipc14' > /dev/null`; then
ipc="14"
fi

if `echo $configName | grep '^seq-sat' > /dev/null`; then
ref_results="$transportroot/datasets/ipc$ipc/results/transport-strips-seq-sat-ipc$ipc-results.json"
fi
if `echo $configName | grep '^seq-opt' > /dev/null`; then
ref_results="$transportroot/datasets/ipc$ipc/results/transport-strips-seq-opt-ipc$ipc-results.json"
fi
if `echo $configName | grep '^tempo-sat' > /dev/null`; then
ref_results="$transportroot/datasets/ipc$ipc/results/transport-numeric-tempo-sat-ipc$ipc-results.json"
fi
if `echo $configName | grep '^netben-opt' > /dev/null`; then
ref_results="$transportroot/datasets/ipc$ipc/results/transport-numeric-netben-opt-ipc$ipc-results.json"
fi

if [ -z "$ref_results" ]; then
    echo "`date -u '+[%H:%M:%S]'` No reference IPC results found, exiting."
else
    echo "`date -u '+[%H:%M:%S]'` Found reference IPC results at: $ref_results, regenerating reports..."
    ipc_results="$resultdir/results-ipc.json"
    python3 scripts/merge-results.py "$resultdir/results.json" "$ref_results" "$ipc_results"
    ipc_reportdir_name="ipc-reports"
    reportdir="$resultdir/$ipc_reportdir_name"
    eval "$JAVA_HOME/bin/java -Dlogging.path=\"$logdir\" -Dtransport.root=\"$transportroot\" -jar $reportGenerator $resultdir/results-ipc.json $ipc_reportdir_name"
    convertToPdf
fi
echo "`date -u '+[%H:%M:%S]'` Added IPC results."
echo "Result dir: $resultdir"
