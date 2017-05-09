#!/bin/bash
set -e

rm -rf target/
. makescript.sh
mkdir target
cp pdf/*.pdf target/
. clean.sh
