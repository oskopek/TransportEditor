#!/usr/bin/env bash
set -e
chmod +x sis-unconvert.sh.txt
./sis-unconvert.sh.txt
find . -name '*.sh' | xargs chmod +x
cd sources/
mvn clean install -DskipTests
cd ..
mkdir bin
cp `find . -wholename '*target/*jar-with-dependencies.jar' | tr '\n' ' '` bin/
