#!/bin/bash
set -e

bash makescript.sh
bash clean.sh
mkdir target
cp pdf/*.pdf target/
