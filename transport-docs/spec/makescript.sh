#!/bin/bash
names="spec"
parameters="-halt-on-error -file-line-error"
#quiet="> /dev/null"

rm -rvf pdf/ build/

mkdir pdf
mkdir build
mkdir backups

cd tex

IFS=" "; for name in $names; do
    pdflatex $parameters "$name".tex $quiet
    #bibtex "$name".aux #:> /dev/null
    pdflatex $parameters "$name".tex $quiet
    pdflatex $parameters "$name".tex $quiet
    pdflatex $parameters "$name".tex $quiet
    mv "$name".pdf ../pdf/
done

cd ..

mv *.aux ../build/ || true
mv *.dvi ../build/ || true
mv *.log ../build/ || true
mv *.blg ../build/ || true
mv *.bbl ../build/ || true
mv *.out ../build/ || true
mv *.toc ../build/ || true
mv *.idx ../build/ || true

mv *~* ../backups/ || true
