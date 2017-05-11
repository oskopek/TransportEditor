#!/bin/bash
set -e

rm -rf log
. clean.sh
. convert-images.sh
cd tex
make all
cd ..
mkdir target
mv tex/*.pdf target/
mkdir log


if ! which verapdf >/dev/null; then
    echo "WARNING: VeraPDF not installed, skipping validation."
    exit 0
fi

echo "Validating PDF/A-2u..."
#verapdf -f 2u --format mrr target/TransportEditor-user-manual.pdf > log/validation-out-user.xml
verapdf -p ../verapdf/uk-profile.xml --format mrr target/TransportEditor-user-manual.pdf > log/validation-out-user.xml
#verapdf -f 2u --format mrr target/TransportEditor-dev-manual.pdf > log/validation-out-dev.xml
verapdf -p ../verapdf/uk-profile.xml --format mrr target/TransportEditor-dev-manual.pdf > log/validation-out-dev.xml
if which xmllint >/dev/null; then
    cat log/validation-out-user.xml | xmllint --format - > log/validation-user.xml
    cat log/validation-out-dev.xml | xmllint --format - > log/validation-dev.xml
fi
verification="`grep -qE 'inValid="0"' log/validation-out-user.xml && grep -qE 'inValid="0"' log/validation-out-dev.xml; echo $?`"
if [ "$verification" -ne 0 ]; then
    echo "PDF validation FAILED!"
    rm -rf log/
    return "$verification"
fi
echo "PDF validation PASSED!"
rm -rf log/
return 0

