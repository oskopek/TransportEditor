#!/bin/bash

rm -rf results/
scp -r oskopek@skirit.ics.muni.cz:/storage/brno2/home/oskopek/git/TransportEditor/tools/benchmarks/results .
exec ./merge-last-results.sh
