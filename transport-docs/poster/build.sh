#!/bin/bash
set -e

rm -rf target/
cd ../bp
./convert-images.sh
cd ../poster
. makescript.sh
mkdir target
cp pdf/*.pdf target/
. clean.sh
cd ../bp
. clean.sh
cd ../poster
