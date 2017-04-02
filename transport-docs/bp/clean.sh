#!/bin/bash
set -e
rm -rvf target/ imga/*.pdf
cd en
make clean || return 1
cd ..
