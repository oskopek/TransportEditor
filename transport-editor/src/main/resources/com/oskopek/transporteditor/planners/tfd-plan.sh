#!/bin/bash
solution="`mktemp`"
solution="`realpath $solution`"
oldpwd="`pwd`"
tfd_dir="$HOME/dev/planners/tfd/tfd-src-0.4/downward"
domain="`realpath $1`"
problem="`realpath $2`"

function cleanup {
cat "$solution"
rm "$solution"
cd "$oldpwd"
exit "$EXIT_STATUS"
}

cd "$tfd_dir"

trap 'pkill -TERM -P $PID; cleanup' SIGINT SIGTERM
. plan "$domain" "$problem" "$solution" 1>&2 &
PID=$!
wait $PID
trap - SIGINT SIGTERM
wait $PID
EXIT_STATUS=$?

cleanup

