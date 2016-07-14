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
    bibtex "$name".aux #:> /dev/null
    pdflatex $parameters "$name".tex $quiet
    pdflatex $parameters "$name".tex $quiet
    pdflatex $parameters "$name".tex $quiet
    mv "$name".pdf ../pdf/
done

mv *.aux ../build/
mv *.dvi ../build/
mv *.log ../build/
mv *.blg ../build/
mv *.bbl ../build/
mv *.out ../build/
mv *.toc ../build/
mv *.idx ../build/

mv *~* ../backups/
