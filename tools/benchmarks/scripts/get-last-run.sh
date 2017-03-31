#!/bin/bash

# Args: configfile

set -e

configFile="$1"


if [ -z "$configFile" ]; then
    echo "Need 1 param: [config_file.json]"
    exit 1
fi

configName="`basename $(realpath $configFile) | sed 's/.json$//'`"
configResultDir="`realpath results/$configName`"

realpath "results/$configName/"`ls "$configResultDir" | tail -n 1`

