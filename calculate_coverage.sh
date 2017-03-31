#!/usr/bin/env bash
set -e

if [ -z "$1" ]; then
reportFolder="jacoco"
else
reportFolder="jacoco-it"
fi

for file in `find . -wholename '.*'"$reportFolder"'/jacoco.xml'`; do
cat $file | grep -E '<counter type="INSTRUCTION"[^>]*/>' -o | tail -n 1 | sed -E 's@<counter type="INSTRUCTION"[ ]+missed="([0-9]+)"[ ]+covered="([0-9]+)"[ ]*/>@\2 (\1+\2)@'
done | # awk transpose script from: http://stackoverflow.com/a/1729980/2713162
awk '
{
    for (i=1; i<=NF; i++)  {
        a[NR,i] = $i
    }
}
NF>p { p = NF }
END {
    for(j=1; j<=p; j++) {
        str=a[1,j]
        for(i=2; i<=NR; i++){
            str=str" "a[i,j];
        }
        print str
    }
}' | tr ' \n' '+/' | sed 's@/$@@;s@^@(@;s@$@)@;s@/@)/(@;s@^@scale=2;@;s@$@\n@' | bc
