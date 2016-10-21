#!/bin/bash
rm -rf target/
cd en
make clean || return 1
cd ..
