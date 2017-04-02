#!/bin/bash
set -e

bash clean.sh
#bash update-biblio.sh
cd en
make all
cd ..
mkdir target
mv en/thesis.pdf target/
verapdf -f 2u --format mrr ~/git/TransportEditor/transport-docs/bp/target/thesis.pdf > target/validation-out.xml
if which xmllint >/dev/null; then
    cat target/validation-out.xml | xmllint --format - > target/validation.xml
fi
verification="`grep -qE 'inValid="0"' target/validation-out.xml; echo $?`"
if [ "$verification" -ne 0 ]; then
    echo "PDF validation failed!"
    exit "$verification"
fi
exit 0

