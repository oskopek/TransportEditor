#!/bin/bash

PID=""

function cleanup {
echo "Fast-downward exited!" 1>&2
cat "`ls sas_plan* | tail -n 1`"
cd "$oldpwd"
rm -rf "$tmpdir"
exit "$EXIT_STATUS"
}

if [ -z "$1" ]; then
    echo "Please supply a domain file as the first argument." 1>&2
    exit
fi
if [ -z "$2" ]; then
    echo "Please supply a problem file as the second argument." 1>&2
    exit
fi

fastdownargs="${@:1}"
oldpwd="`pwd`"
tmpdir="`mktemp`"
rm -rf "$tmpdir"
mkdir -p "$tmpdir"
cd "$tmpdir"
echo fast-downward "$fastdownargs" 1>&2

trap 'pkill -TERM -P $PID; cleanup' SIGINT SIGTERM
fast-downward --build release64 $fastdownargs 1>&2 &
PID=$!
wait $PID
trap - SIGINT SIGTERM
wait $PID
EXIT_STATUS=$?

cleanup

