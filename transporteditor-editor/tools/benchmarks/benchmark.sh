#!/bin/bash
if [ -z "$transportroot" ]; then
transportroot="`realpath "../../"`"
fi
if [ -z "$benchmarker" ]; then
benchmarker="$transportroot/target/TransportEditor-Benchmarker-jar-with-dependencies.jar"
fi

set -e
if [ -z "$1" ]; then
    echo "Please supply a benchmark config file as the first argument." 1>&2
    exit
fi
config="`realpath "$1"`"
configName="`basename $config | sed 's/\.json//'`"
starttime="`date -u '+%Y%m%d-%H%M%S'`"
origdir="`pwd`"

resultdir="$origdir/results/$configName/$starttime"
mkdir -p "$resultdir"
cp "$config" "$resultdir"
export logdir="$resultdir"

eval "$JAVA_HOME/bin/java -Dlogging.path=\"$logdir\" -Dtransport.root=\"$transportroot\" -jar $benchmarker $config > $resultdir/results.json"

endtime="`date -u '+%Y%m%d-%H%M%S'`"
