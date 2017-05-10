#!/bin/bash

if [ -z "$transportroot" ]; then
transportroot="`realpath "../../"`"
if ls "$transportroot" | grep "sources" >/dev/null; then
transportroot="$transportroot/sources"
fi
fi
if [ -z "$benchmarker" ]; then
benchmarker="$transportroot/transport-benchmark/target/""`ls "$transportroot/transport-benchmark/target/" | grep 'transport-benchmark-.*-jar-with-dependencies.jar' | tail -n 1`"
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

eval "$JAVA_HOME/bin/java -Xmx20g -server -XX:GCTimeRatio=19 -Xms4g -XX:ParallelGCThreads=5 -Dlogging.path=\"$logdir\" -Dtransport.root=\"$transportroot\" -jar $benchmarker $config $resultdir"
endtime="`date -u '+%Y%m%d-%H%M%S'`"

echo "End time UTC: $endtime"
echo "Results: $resultdir"

if [ -n "$DISPLAY" ]; then
echo "Generating reports..."
exec ./generate-reports.sh  "$config" "$resultdir/results.json"
else
echo "Not generating reports, no display found."
fi
