#!/bin/bash
oldpwd="`pwd`"
filename="skopeko-nprg.zip"

mkdir data
cd ..
rm "$filename"
cp datasets/ipc08/seq-opt/transport-strips/p??.pddl "$oldpwd"/data/
zip -r "$filename" nprg/
zip -d "$filename" nprg/package.sh

cd "$oldpwd"
rm -rf data
