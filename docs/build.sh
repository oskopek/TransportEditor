#!/bin/bash

origdir="`pwd`"
projectdir="`realpath "$origdir/.."`"/target/docs
subdirs="spec diary bp"

rm -rf "$projectdir"
mkdir -p "$projectdir"

for dir in $subdirs; do
    cd "$dir"
    bash build.sh

    cd "$projectdir"
    mkdir "$dir"
    cd "$dir"
    cp -r "$origdir"/"$dir"/target/* .

    cd "$origdir"/"$dir"
    bash clean.sh
    cd "$origdir"
done

