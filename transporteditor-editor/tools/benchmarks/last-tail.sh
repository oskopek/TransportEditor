#!/bin/bash

# Args: configfile

set -e

configFile="$1"

if [ -z "$configFile" ]; then
    echo "Need 1 param: [config_file.json]"
    exit 1
fi

lastRun="`sh scripts/get-last-run.sh $configFile`"
logFile="`ls $lastRun/log* | tail -n 1`"
grepArg='] INFO'

cat "$logFile" | grep "$grepArg"

if `tail -n 1 "$logFile" | grep 'All benchmarks finished' >/dev/null`; then
    exit 0
fi

tail -n 0 -f "$logFile" | grep --line-buffered "$grepArg"

