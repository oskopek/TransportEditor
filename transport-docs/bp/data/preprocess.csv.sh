#!/bin/bash
for csv in `ls | grep '\.csv$'`; do
dos2unix "$csv"
sed -E 's/[,]+$/\\\\/;s/,[0-9]+\.[0-9]+E\+[0-9]+//' "$csv" > a
mv a "$csv"
done
