#!/bin/bash
set -e

bash clean.sh
cd en
make all
cd ..
mkdir target
cp en/thesis.pdf target/
