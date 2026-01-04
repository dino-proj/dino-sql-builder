#!/usr/bin/env bash
# Copyright 2024 dinosdev.cn.
# SPDX-License-Identifier: Apache-2.0


bin=`dirname "${BASH_SOURCE-$0}"`
CWD=`cd "$bin"; pwd`

function usage(){
  echo "USAGE:"
  echo "release-version <new version>"
  echo ""
  echo "example:"
  echo "release-version 1.1.1"
}

function isMac(){
  a=`uname  -a`
  return $a =~ "Darwin"
}

if [ $# -ne 1 ];
then
    usage
    exit
fi

NEW_VER=$1

mvn versions:set -DnewVersion=${NEW_VER} -DgenerateBackupPoms=false
mvn install 

# change version in ./README.md
echo "change version in README.md"
cd "$CWD/.."
# Update Maven dependency version
sed -i '' -E '/<parent>/,/<\/parent>/ s/(<version>)[^<]+(<\/version>)/\1'"$NEW_VER"'\2/' README.md
# Update Gradle dependency version
sed -i '' -E "s/(implementation 'cn\.dinodev:dino-sql-builder:)[0-9.]+'/\1$NEW_VER'/" README.md



# ask for confirmation to push
read -p "Do you want to push the changes to Github? (y/n) " -n 1 -r
echo   # (optional) move to a new line
if [[ ! $REPLY =~ ^[Yy]$ ]]
then
    echo "exit without push"
    exit
fi

# git commit
echo "git commit"
git add .
git commit -m "release version $NEW_VER"

# git tag
echo "git tag"
git tag -a "RELEASE-$NEW_VER" -m "release version $NEW_VER"

# git push main and tag
echo "git push"
git push
git push origin "RELEASE-$NEW_VER"

echo "done!"

