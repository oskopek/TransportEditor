#!/usr/bin/env bash

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

oldVersion=`cat pom.xml | grep '<version>' | head -n 1 | grep -Eo '[.0-9]+[^<]*'`
echo "Current version: $oldVersion"
relVersion=`input "Please enter the release version number: "`
relBranch=`input "Please enter the release branch name: "`
newVersion=`input "Please enter the new version number: "`
newRelBranch=`input "Please enter the release branch new version number: "`
tagName="v$relVersion"

echo
echo "Current version: $oldVersion"
echo "Release version: $relVersion"
echo "Release branch: $relBranch"
echo "Release branch new version: $newRelBranch"
echo "Tag name: $tagName"
echo "New version: $newVersion"
echo

echo "======================================================="
echo "|              DANGER: HERE BE DRAGONS                |"
echo "======================================================="
echo

confirm "Are you sure you want to release using these version numbers?" || exit

echo "Starting release..."

git checkout -b "$relBranch"
mvn versions:set -DnewVersion="$relVersion" -DgenerateBackupPoms=false -P"$MVN_PROFILES"
git add '**/pom.xml' 'pom.xml'
git commit -m "Bumping version to $relVersion"

git tag -a "$tagName" -m "Release $relVersion"

tmpdir="TEMP`date +%s`"
mkdir "$tmpdir"
mvn clean install -P"$MVN_PROFILES"
mvn install -Pbenchmarker # TODO: Remove me

mkdir -p "$tmpdir"/datasets
cp -r datasets/description.txt datasets/ipc* "$tmpdir"/datasets/

cp -r transporteditor-docs/target/docs "$tmpdir"/

mkdir -p "$tmpdir"/docs/javadoc
cp -r transporteditor-editor/target/apidocs/* "$tmpdir"/docs/javadoc/

rsync -av --exclude='*/.git*' --exclude 'target/' --exclude 'tools/' transporteditor-editor/ "$tmpdir"/sources
cp -r transporteditor-editor/target/TransportEditor-jar-with-dependencies.jar "$tmpdir"/

cp -r transporteditor-editor/tools "$tmpdir"/
cp -r transporteditor-editor/target/*Benchmarker*dependencies.jar "$tmpdir"/tools/benchmarks
rm -rf "$tmpdir"/tools/benchmarks/results

cp README.adoc LICENSE "$tmpdir"/
cp "NOTICE.adoc" "AUTHORS.adoc" "$tmpdir"/

relName="TransportEditor-$relVersion"
mv "$tmpdir" "$relName"
zip -r "$relName".zip "$relName"
rm -rf "$relName"
mvn clean -P"$MVN_PROFILES"

mvn versions:set -DnewVersion="$newRelBranch" -DgenerateBackupPoms=false -P"$MVN_PROFILES"
git add '**/pom.xml' 'pom.xml'
git commit -m "Bumping release branch version to $newRelBranch"

git checkout "$base_branch"

mvn versions:set -DnewVersion="$newVersion" -DgenerateBackupPoms=false -P"$MVN_PROFILES"
git add '**/pom.xml' 'pom.xml'
git commit -m "Bumping version to $newVersion"
