#!/bin/bash

# Install git-lfs and pull the data files

LFS_VERSION=1.4.3
build_dir="$(realpath $(pwd))"
#domain="github.com"
domain="192.30.253.113"
cd /tmp

echo 'install git lfs'
filename=git-lfs-linux-amd64-"$LFS_VERSION".tar.gz
wget https://$domain/github/git-lfs/releases/download/v"$LFS_VERSION"/"$filename" --no-check-certificate
tar -zxvf "$filename"
cd "./git-lfs-$LFS_VERSION"
. ./install.sh

cd "$build_dir"
git lfs install

echo 'git reset'
git reset

echo 'pulling from git-lfs'
git lfs pull
