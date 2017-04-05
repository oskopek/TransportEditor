#!/bin/bash
set -e
rm -rvf target/ imga/*.pdf
cd tex
make clean || return 1
cd ..
