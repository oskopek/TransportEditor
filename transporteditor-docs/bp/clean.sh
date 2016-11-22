#!/bin/bash
set -e
rm -rf target/
cd en
make clean || return 1
cd ..
