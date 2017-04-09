#!/bin/bash
files="$@"
echo "Problem, Vehicles, Packages, Locations, Roads, PetrolStations"
for f in $files; do
    name=`basename $f | sed 's/\.pddl//'`
    vehicles="`grep -E '\- vehicle' $f -c`"
    locations="`grep -E '\- location' $f -c`"
    packages="`grep -E '\- package' $f -c`"
    roads="`grep -E '\(road ' $f -c`"
    capacity="`grep -E 'has-petrol-station' $f -c`"
    echo "$name, $vehicles, $packages, $locations, $roads, $capacity"
done

