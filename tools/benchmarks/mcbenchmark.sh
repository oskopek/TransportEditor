#!/usr/bin/env bash
export HOME=/storage/brno2/home/oskopek
source $HOME/.bashrc
export JAVA_HOME=$HOME/java/latest
config="$1"
TEvariant="TransportEditor"
cd $HOME/git/$TEvariant/tools/benchmarks
exec ./benchmark.sh "$config"
