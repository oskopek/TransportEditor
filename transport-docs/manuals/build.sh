#!/bin/bash
set -e

. clean.sh
. convert-images.sh
cd tex
make all
cd ..
mkdir target
mv tex/*.pdf target/


if ! which verapdf >/dev/null; then
    echo "WARNING: VeraPDF not installed, skipping validation."
    exit 0
fi

echo "Validating PDF/A-2u..."
verapdf -f 2u --format mrr ~/git/TransportEditor/transport-docs/manuals/target/TransportEditor-user-manual.pdf > target/validation-out-user.xml
verapdf -f 2u --format mrr ~/git/TransportEditor/transport-docs/manuals/target/TransportEditor-dev-manual.pdf > target/validation-out-dev.xml
if which xmllint >/dev/null; then
    cat target/validation-out-user.xml | xmllint --format - > target/validation-user.xml
    cat target/validation-out-dev.xml | xmllint --format - > target/validation-dev.xml
fi
verification="`grep -qE 'inValid="0"' target/validation-out-user.xml && grep -qE 'inValid="0"' target/validation-out-dev.xml; echo $?`"
if [ "$verification" -ne 0 ]; then
    echo "PDF validation FAILED!"
    exit "$verification"
fi
echo "PDF validation PASSED!"
exit 0

