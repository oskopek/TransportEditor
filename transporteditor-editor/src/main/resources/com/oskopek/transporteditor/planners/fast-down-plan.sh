#!/bin/bash
set -e
if [ -z "$1" ]; then
    echo "Please supply a domain file as the first argument." 1>&2
    exit
fi
if [ -z "$2" ]; then
    echo "Please supply a problem file as the second argument." 1>&2
    exit
fi
fastdownwardargs="${@:3}"
oldpwd="`pwd`"
tmpdir="`mktemp`"
rm -rf "$tmpdir"
mkdir -p "$tmpdir"
domain="`realpath "$1"`"
problem="`realpath "$2"`"
cd "$tmpdir"
echo fast-downward "$domain" "$problem" "$fastdownwardargs" 1>&2
fast-downward "$domain" "$problem" $fastdownwardargs 1>&2
cat sas_plan
cd "$oldpwd"
rm -rf "$tmpdir"

