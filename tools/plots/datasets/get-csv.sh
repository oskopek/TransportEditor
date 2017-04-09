#!/bin/bash
files="$@"
echo "Problem, Vehicles, Packages, Locations, Roads, MaxCapacity"
for f in $files; do
    name=`basename $f | sed 's/\.pddl//'`
    vehicles="`grep -E '\- vehicle' $f -c`"
    locations="`grep -E '\- location' $f -c`"
    packages="`grep -E '\- package' $f -c`"
    roads="`grep -E '\(road ' $f -c`"
    capacity="`grep -E '.*\- capacity-number' $f -o | grep -E '[0-9]+' -o | sort -n | tail -n 1`"
    echo "$name, $vehicles, $packages, $locations, $roads, $capacity"
done

