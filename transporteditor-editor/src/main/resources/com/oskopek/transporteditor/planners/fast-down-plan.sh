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
oldpwd="`pwd`"
tmpdir="`mktemp`"
rm -rf "$tmpdir"
mkdir -p "$tmpdir"
domain="`realpath "$1"`"
problem="`realpath "$2"`"
cd "$tmpdir"
fast-downward "$domain" "$problem"  1>&2
cat sas_plan
cd "$oldpwd"
rm -rf "$tmpdir"

