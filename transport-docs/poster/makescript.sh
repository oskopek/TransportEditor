#!/bin/bash
names="poster"
parameters="-halt-on-error -file-line-error"
#quiet="> /dev/null"

set +e

rm -rvf pdf/ build/

mkdir pdf
mkdir build
mkdir backups

set -e

cd tex

IFS=" "; for name in $names; do
    pdflatex $parameters "$name".tex $quiet
    pdflatex $parameters "$name".tex $quiet
    mv "$name".pdf ../pdf/
done

mv *.aux ../build/ || true
mv *.nav ../build/ || true
mv *.snm ../build/ || true
mv *.dvi ../build/ || true
mv *.log ../build/ || true
mv *.blg ../build/ || true
mv *.bbl ../build/ || true
mv *.out ../build/ || true
mv *.toc ../build/ || true
mv *.idx ../build/ || true

mv *~* ../backups/ || true
cd ..
