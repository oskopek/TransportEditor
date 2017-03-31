#!/bin/bash

PID=""

function cleanup {
echo "Fast-downward exited!"
cat "`ls sas_plan* | tail -n 1`" > "$output"
cd "$oldpwd"
rm -rf "$tmpdir"
exit "$EXIT_STATUS"
}

if [ -z "$1" ]; then
    echo "Please supply an output file as the first argument." 1>&2
    exit 1
fi
if [ -z "$2" ]; then
    echo "Please supply a domain file as the second argument." 1>&2
    exit 1
fi
if [ -z "$3" ]; then
    echo "Please supply a problem file as the third argument." 1>&2
    exit 1
fi

output="`realpath $1`"
fastdownargs="${@:2}"
oldpwd="`pwd`"
tmpdir="`mktemp`"
rm -rf "$tmpdir"
mkdir -p "$tmpdir"
cd "$tmpdir"
echo fast-downward "$fastdownargs"

trap 'pkill -TERM -P $PID; cleanup' SIGINT SIGTERM
fast-downward $fastdownargs &
PID=$!
wait $PID
trap - SIGINT SIGTERM
wait $PID
EXIT_STATUS=$?

cleanup
