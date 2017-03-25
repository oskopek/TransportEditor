#!/bin/bash

function convertToPdf {
if `which rsvg-convert >/dev/null`; then
    for svg in `ls $reportdir/*.svg`; do
        filename="$reportdir"/"`basename $svg | sed 's/\.svg//g'`"
        rsvg-convert -f pdf -o "$filename".pdf "$filename".svg
    done
fi
}

if [ -z "$transportroot" ]; then
transportroot="`realpath "../../"`"
fi
if [ -z "$benchmarker" ]; then
benchmarker="$transportroot/target/TransportEditor-Benchmarker-jar-with-dependencies.jar"
fi
if [ -z "$reportGenerator" ]; then
reportGenerator="$transportroot/target/TransportEditor-ReportGenerator-jar-with-dependencies.jar"
fi

set -e
if [ -z "$1" ]; then
    echo "Please supply a benchmark config file as the first argument." 1>&2
    exit
fi
config="`realpath "$1"`"
configName="`basename $config | sed 's/\.json//'`"
starttime="`date -u '+%Y%m%d-%H%M%S'`"
echo "Start time UTC: $starttime"
origdir="`pwd`"

resultdir="$origdir/results/$configName/$starttime"
mkdir -p "$resultdir"
logdir="$resultdir"

eval "$JAVA_HOME/bin/java -Dlogging.path=\"$logdir\" -Dtransport.root=\"$transportroot\" -jar $benchmarker $config $resultdir"

endtime="`date -u '+%Y%m%d-%H%M%S'`"

echo "End time UTC: $endtime"
duration="$(date -d@$(( ( $(date -ud '2003-08-02 17:24:33' +'%s') - $(date -ud '2003-04-21 22:55:02' +'%s') ) )) +'%m months %d days %H hours %M minutes %S seconds')"
echo "Duration: $duration"

echo "`date -u '+[%H:%M:%S]'` Converting plots..."
reportdir="$resultdir"/reports
convertToPdf
echo "`date -u '+[%H:%M:%S]'` Plots converted."

echo "Result dir: $resultdir"


echo "`date -u '+[%H:%M:%S]'` Adding IPC results..."

if `echo $configName | grep 'ipc08' > /dev/null`; then
ipc="08"
fi
if `echo $configName | grep 'ipc14' > /dev/null`; then
ipc="14"
fi

if `echo $configName | grep '^seq-sat' > /dev/null`; then
ref_results="$transportroot/../datasets/ipc$ipc/results/transport-strips-seq-sat-ipc$ipc-results.json"
fi
if `echo $configName | grep '^seq-opt' > /dev/null`; then
ref_results="$transportroot/../datasets/ipc$ipc/results/transport-strips-seq-opt-ipc$ipc-results.json"
fi
if `echo $configName | grep '^tempo-sat' > /dev/null`; then
ref_results="$transportroot/../datasets/ipc$ipc/results/transport-numeric-tempo-sat-ipc$ipc-results.json"
fi
if `echo $configName | grep '^netben-opt' > /dev/null`; then
ref_results="$transportroot/../datasets/ipc$ipc/results/transport-numeric-netben-opt-ipc$ipc-results.json"
fi

if [ -z "$ref_results" ]; then
    echo "`date -u '+[%H:%M:%S]'` No reference IPC results found, exiting."
else
    echo "`date -u '+[%H:%M:%S]'` Found reference IPC results at: $ref_results, regenerating reports..."
    ipc_results="$resultdir/results-ipc.json"
    python3 scripts/merge-results.py "$resultdir/results.json" "$ref_results" "$ipc_results"
    ipc_reportdir_name="ipc-reports"
    eval "$JAVA_HOME/bin/java -Dlogging.path=\"$logdir\" -Dtransport.root=\"$transportroot\" -jar $reportGenerator $resultdir/results-ipc.json $ipc_reportdir_name"
    convertToPdf
fi
echo "`date -u '+[%H:%M:%S]'` Added IPC results."
echo "Result dir: $resultdir"