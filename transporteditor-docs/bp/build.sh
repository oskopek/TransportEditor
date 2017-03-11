#!/bin/bash
set -e

bash clean.sh
bash update-biblio.sh
cd en
make all
cd ..
mkdir target
mv en/thesis.pdf target/
