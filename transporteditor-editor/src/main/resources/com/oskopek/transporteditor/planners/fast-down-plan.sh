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
fastdownargs="${@:1}"
oldpwd="`pwd`"
tmpdir="`mktemp`"
rm -rf "$tmpdir"
mkdir -p "$tmpdir"
cd "$tmpdir"
echo fast-downward "$fastdownargs" 1>&2
fast-downward $fastdownargs 1>&2
cat "`ls sas_plan* | tail -n 1`"
cd "$oldpwd"
rm -rf "$tmpdir"

