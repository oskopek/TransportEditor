#!/bin/bash

# Packaging script. Removes this script and the TODO file from the archive.

# No parameters.

##########################################################################

oldpwd="`pwd`"
filename="skopeko-nprg.zip"
ipcdest="$oldpwd/inputs/ipc08"

cd ..
rm "$filename"
mkdir -p "$ipcdest"
cp datasets/ipc08/seq-opt/transport-strips/p??.pddl "$ipcdest"/
zip -r "$filename" nprg/
zip -d "$filename" nprg/package.sh
zip -d "$filename" nprg/TODO.adoc

cd "$oldpwd"
rm -rf "$ipcdest"
