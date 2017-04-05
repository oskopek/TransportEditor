#!/bin/bash
set -e

rm -rf log
. clean.sh
#. update-biblio.sh
. convert-images.sh
cd en
make all
cd ..
mkdir target
mv en/thesis.pdf target/
mkdir log

if ! which verapdf >/dev/null; then
    echo "WARNING: VeraPDF not installed, skipping validation."
    exit 0
fi

echo "Validating PDF/A-2u..."
verapdf -f 2u --format mrr ~/git/TransportEditor/transport-docs/bp/target/thesis.pdf > log/validation-out.xml
if which xmllint >/dev/null; then
    cat log/validation-out.xml | xmllint --format - > log/validation.xml
fi
verification="`grep -qE 'inValid="0"' log/validation-out.xml; echo $?`"
if [ "$verification" -ne 0 ]; then
    echo "PDF validation FAILED!"
    return "$verification"
fi
echo "PDF validation PASSED!"
return 0

