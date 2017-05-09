#!/bin/bash
set -e

. makescript.sh
. clean.sh
mkdir target
cp pdf/*.pdf target/
