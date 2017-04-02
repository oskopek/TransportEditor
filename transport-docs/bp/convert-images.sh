#!/bin/bash
whitelist="logo-en"
for img in `ls img | grep '.pdf' | sed 's/\.pdf$//'`; do
    skip=""
    for white in $whitelist; do
        if [ "$white" == "$img" ]; then
            skip="true"
        fi
    done

    if [ -z "$skip" ]; then
        echo "Converting img/$img.pdf to imga/$img.pdf..."
        echo
        gs -dPDFA -dBATCH -dNOPAUSE -sProcessColorModel=DeviceCMYK -sDEVICE=pdfwrite -sPDFACompatibilityPolicy=1 -sOutputFile=imga/"$img".pdf img/"$img".pdf
        echo
        echo
    fi
done
