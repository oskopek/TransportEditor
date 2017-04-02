#!/bin/bash
set -e

. clean.sh
#. update-biblio.sh
. convert-images.sh
cd en
make all
cd ..
mkdir target
mv en/thesis.pdf target/


if ! which verapdf >/dev/null; then
    echo "WARNING: VeraPDF not installed, skipping validation."
    exit 0
fi

echo "Validating PDF/A-2u..."
verapdf -f 2u --format mrr ~/git/TransportEditor/transport-docs/bp/target/thesis.pdf > target/validation-out.xml
if which xmllint >/dev/null; then
    cat target/validation-out.xml | xmllint --format - > target/validation.xml
fi
verification="`grep -qE 'inValid="0"' target/validation-out.xml; echo $?`"
if [ "$verification" -ne 0 ]; then
    echo "PDF validation FAILED!"
    exit "$verification"
fi
echo "PDF validation PASSED!"
exit 0

