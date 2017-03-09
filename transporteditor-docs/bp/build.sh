#!/bin/bash
set -e

bash clean.sh
cd en
make all
cd ..
mkdir target
mv en/thesis.pdf target/
