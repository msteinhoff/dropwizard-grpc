#!/bin/bash
STAGING_DIRECTORY=$HOME/ghp-staging
REPO_SLUG=msteinhoff/dropwizard-grpc
BRANCH=master
JDK_VERSION=oraclejdk8

[ -z "$GH_TOKEN" ] && { echo "Skipping build because GH_TOKEN not set"; exit; }
[ "$TRAVIS_PULL_REQUEST" == "false" ] || { echo "Skipping build, not publishing gh-pages for pull request"; exit; }
[ "$TRAVIS_REPO_SLUG" == "$REPO_SLUG" ] || { echo "Skipping build, expected TRAVIS_REPO_SLUG to be $REPO_SLUG but was $TRAVIS_REPO_SLUG"; exit; }
[ "$TRAVIS_BRANCH" == "$BRANCH" ] || { echo "Skipping build, expected TRAVIS_BRANCH to be $BRANCH but was $TRAVIS_BRANCH"; exit; }
[ "$TRAVIS_JDK_VERSION" == "$JDK_VERSION" ] || { echo "Skipping build, expected TRAVIS_JDK_VERSION to be $JDK_VERSION but was $TRAVIS_JDK_VERSION"; exit; }

. version.properties

[ -z "$DROPWIZARD_VERSION" ] && { "Skipping build because DROPWIZARD_VERSION not set"; exit; }
[ -z "$PROJECT_VERSION" ] && { "Skipping build because PROJECT_VERSION not set"; exit; }
[ -z "$SNAPSHOT" ] && { "Skipping build because SNAPSHOT not set"; exit; }

echo -e "Publishing README and CHANGELOG...\n"
mkdir -p $STAGING_DIRECTORY
cp _config.yml $STAGING_DIRECTORY
cp README.md $STAGING_DIRECTORY
cp CHANGELOG.md $STAGING_DIRECTORY

if [ "$SNAPSHOT" = "false" ];
then
    RELEASE_VERSION="$DROPWIZARD_VERSION-$PROJECT_VERSION"
    echo -e "Building javadoc...\n"
    ./gradlew javadoc

    echo -e "Staging findbugs, pmd, junit, jacodo reports and javadoc...\n"
    mkdir -p $STAGING_DIRECTORY/$RELEASE_VERSION
    cp -R build/reports/findbugs $STAGING_DIRECTORY/$RELEASE_VERSION/findbugs
    cp -R build/reports/pmd $STAGING_DIRECTORY/$RELEASE_VERSION/pmd
    cp -R build/reports/tests/test $STAGING_DIRECTORY/$RELEASE_VERSION/tests
    cp -R build/reports/jacoco $STAGING_DIRECTORY/$RELEASE_VERSION/jacoco
    cp -R build/docs/javadoc $STAGING_DIRECTORY/$RELEASE_VERSION/javadoc
fi

echo -e "Cloning gh-pages branch...\n"
cd $HOME
git config --global user.email "travis@travis-ci.org"
git config --global user.name "travis-ci"
git clone --quiet --branch=gh-pages https://$GH_TOKEN@github.com/$REPO_SLUG.git ghp-repo > /dev/null

echo -e "Updating gh-pages branch...\n"
cd ghp-repo
git rm -f ./*
cp -f $STAGING_DIRECTORY/* .

if [ "$SNAPSHOT" = "false" ];
then
    mkdir $RELEASE_VERSION/
    cp -Rf $STAGING_DIRECTORY/$RELEASE_VERSION/* $RELEASE_VERSION/
fi

git add -A -f .
git commit -m "Latest page content on successful travis build $TRAVIS_BUILD_NUMBER auto-pushed to gh-pages"
git push -fq origin gh-pages > /dev/null

echo -e "Published website to gh-pages.\n"
