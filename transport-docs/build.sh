#!/bin/bash

set -e

origdir="`pwd`"
projectdir="$origdir"/target/docs/
subdirs="spec diary bp manuals poster"

rm -rf "$projectdir"
mkdir -p "$projectdir"

for dir in $subdirs; do
    cd "$dir"
    echo "Building $dir..."
    . build.sh
    echo "Build $dir done."

    cd "$projectdir"
    mkdir "$dir"
    cd "$dir"
    cp -vr "$origdir"/"$dir"/target/* .

    cd "$origdir"/"$dir"
    echo "Cleaning $dir..."
    . clean.sh
    cd "$origdir"
done

echo "Done!"

# Report:
echo "Generating report..."
report_out="`mktemp`"
. report.sh > $report_out
mv "$report_out" "$projectdir"/report.html
