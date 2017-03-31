#!/bin/bash

set -e

bash clean.sh
asciidoctor *.adoc
mkdir target
mv *.html target/
cp *.pdf target/

