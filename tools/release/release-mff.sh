#!/usr/bin/env bash

set -e

MVN_PROFILES="it,docs"
base_branch="`git status | grep "On branch" | sed -E 's/On branch\s+(.*)/\1/' | tr -d '[:blank:]'`"

function confirm {
    while true; do
        read -p "$1"" [yN]" yn
        case $yn in
            [Yy] ) return 0;;
            [Nn] ) return 1;;
            * ) echo -e "Please answer \"y\" for yes or \"n\" for no.";;
        esac
    done
}

function input {
    read -p "$1" result
    echo "$result"
}

echo "Testing base branch $base_branch..."

mvn clean install -P"$MVN_PROFILES"
testsPassed=$?

mvn clean -P"$MVN_PROFILES"

echo

if [ $testsPassed -ne 0 ]; then
    echo "Tests failed, aborting release"
    exit
else
    echo "Tests passed"
fi

echo

version=`cat pom.xml | grep '<version>' | head -n 1 | grep -Eo '[.0-9]+[^<]*'`

echo "Current version: $version"
echo

echo "======================================================="
echo "|              DANGER: HERE BE DRAGONS                |"
echo "======================================================="
echo

confirm "Are you sure you want to release using these version numbers?" || exit

echo "Starting release..."

tmpdir="TEMP`date +%s`"
mkdir "$tmpdir"
mvn clean install -P"$MVN_PROFILES" -DskipTests

mkdir -p "$tmpdir"/datasets
cp -r datasets/description.txt datasets/ipc* "$tmpdir"/datasets/

cp -r transport-docs/target/docs "$tmpdir"/

mkdir -p "$tmpdir"/docs/javadoc
for apidocs in `find . -wholename '*target/apidocs' -type d`; do
    cp -r "$apidocs" "$tmpdir"/docs/javadoc/"`echo $apidocs | grep -E 'transport-[a-z]+' -o`"
done

for module in `ls | grep 'transport-.*'`; do
    if [ "$module" = "transport-docs" ]; then
        continue
    fi
    mkdir -p "$tmpdir/sources/$module"
    rsync -av --exclude='*/.git*' --exclude='.idea/' --exclude='*.iml' --exclude 'target/' "$module"/ "$tmpdir"/sources/"$module"
done
cp -r "config/" "pom.xml" "$tmpdir/sources/"

mkdir -p "$tmpdir"/bin
cp `find . -wholename '*target/*jar-with-dependencies.jar' | tr '\n' ' '` "$tmpdir"/bin

cp -r tools "$tmpdir"/
rm -rf "$tmpdir"/tools/benchmarks/results
mkdir -p "$tmpdir"/tools/benchmarks/results

cp "NOTICE.adoc" "AUTHORS.adoc" "LICENSE.txt" "README.adoc" "$tmpdir"/
cp "tools/release/SIS-README.txt" "tools/release/build-mff.sh" "$tmpdir/"
rm -rf "$tmpdir/bin/"
./tools/release/sis-convert.sh "$tmpdir"/

relName="TransportEditor-$version"
mv "$tmpdir" "$relName"
zip -r "$relName".zip "$relName"
rm -rf "$relName"
mvn clean -P"$MVN_PROFILES"
