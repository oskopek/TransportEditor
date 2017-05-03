#!/bin/bash
set -e

function killif {
# Originally: https://askubuntu.com/a/54753/647709
if [ $# -ne 2 ];
then
    echo "Invalid number of arguments."
    exit 0
fi

while true;
do
    memperc=$(ps -eo pid,ppid,pgid,comm,%cpu,%mem | grep " $1 " | tr -s ' ' | cut -d ' ' -f 6 | tr '\n' '+' | sed 's/^/scale=10;(/;s/+$/)\/100\n/' | bc);
    memtotalb=$(awk '/MemTotal/ {print $2}' /proc/meminfo)
    memb=$(echo "$memperc*$memtotalb*1024" | bc)
    membi=$(echo $memb | sed 's/\..*//')
    SIZEMB=$((membi/1024/1024))
    echo "Process id =$1 Size = $SIZEMB MB"
    if [ $SIZEMB -gt $2 ]; then
        printf "SIZE has exceeded the limit $2 MB.\nKilling the process..\n"
        pkill -TERM -P "$1"
        echo "Killed the process."
        exit 0
    else
        echo "SIZE has not yet exceeded limit $2 MB."
    fi

    sleep 5
done
}

function cleanup {
plan_dir="$1"
old_dir="$2"
cd "$plan_dir"
echo "Writing solution to: $output"
cat $(ls "$solution"* | tail -n 1) > "$output"
rm "$solution"*
rm "output" "output.sas" "all.groups" "variables.groups"
cd "$old_dir"
rm -rf "$plan_dir"
exit "$EXIT_STATUS"
}

function plan {
tfd_dir="$1"
domain="$2"
problem="$3"
solution="$4"
config="y+Y+a+e+r+O+1+C+1+b"

if [ $# != 4 ]; then
    echo "Usage: plan <tfdDir> <domainFile> <problemFile> <solutionFile>"
else
python "$tfd_dir"/translate/translate.py "$domain" "$problem"
"$tfd_dir"/preprocess/preprocess < "output.sas"
"$tfd_dir"/search/search `echo -n $config | tr '+' ' '` "p" "$solution" < "output"
fi

}

old_dir="`pwd`"
plan_dir="`mktemp`"
rm "$plan_dir"
mkdir -p "$plan_dir"
solution="`mktemp`"
solution="`realpath $solution`"
tfd_dir="$HOME/dev/planners/tfd/tfd-src-0.4/downward"
domain="`realpath $1`"
problem="`realpath $2`"
output="`realpath $3`"

EXIT_STATUS="0"

trap 'pkill -TERM -P $PID; cleanup $plan_dir $old_dir' SIGINT SIGTERM SIGHUP
cd "$plan_dir" && plan "$tfd_dir" "$domain" "$problem" "$solution" &
PID=$!
killif $PID 2048 &
wait $PID
trap - SIGINT SIGTERM
wait $PID
EXIT_STATUS=$?

cleanup

