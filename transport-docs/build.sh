#!/bin/bash

set -e

origdir="`pwd`"
projectdir="$origdir"/target/docs/
subdirs="spec diary bp"

rm -rf "$projectdir"
mkdir -p "$projectdir"

for dir in $subdirs; do
    cd "$dir"
    . build.sh

    cd "$projectdir"
    mkdir "$dir"
    cd "$dir"
    cp -r "$origdir"/"$dir"/target/* .

    cd "$origdir"/"$dir"
    . clean.sh
    cd "$origdir"
done

# Report:
echo "Generating report..."
report_out="`mktemp`"
. report.sh > $report_out
mv "$report_out" "$projectdir"/report.html
