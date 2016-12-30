#!/bin/bash
REPO_SLUG=msteinhoff/dropwizard-grpc
BRANCH=master
JDK_VERSION=oraclejdk8

[ -z "$GH_TOKEN" ] && { echo "Skipping build because GH_TOKEN not set"; exit; }
[ "$TRAVIS_PULL_REQUEST" == "false" ] || { echo "Skipping build, not publishing gh-pages for pull request"; exit; }
[ "$TRAVIS_REPO_SLUG" == "$REPO_SLUG" ] || { echo "Skipping build, expected TRAVIS_REPO_SLUG to be $REPO_SLUG but was $TRAVIS_REPO_SLUG"; exit; }
[ "$TRAVIS_BRANCH" == "$BRANCH" ] || { echo "Skipping build, expected TRAVIS_BRANCH to be $BRANCH but was $TRAVIS_BRANCH"; exit; }
[ "$TRAVIS_JDK_VERSION" == "$JDK_VERSION" ] || { echo "Skipping build, expected TRAVIS_JDK_VERSION to be $JDK_VERSION but was $TRAVIS_JDK_VERSION"; exit; }

echo -e "Publishing website to gh-pages.\n"
mkdir -p $HOME/ghp-staging

echo -e "Building javadoc...\n"
./gradlew javadoc

echo -e "Publishing README and CHANGELOG...\n"
cp _config.yml $HOME/ghp-staging
cp README.md $HOME/ghp-staging
cp CHANGELOG.md $HOME/ghp-staging

echo -e "Publishing reports and javadoc...\n"
cp -R build/reports/findbugs $HOME/ghp-staging/findbugs
cp -R build/reports/pmd $HOME/ghp-staging/pmd
cp -R build/reports/tests/test $HOME/ghp-staging/tests
cp -R build/reports/jacoco $HOME/ghp-staging/jacoco
cp -R build/docs/javadoc $HOME/ghp-staging/javadoc

echo -e "Cloning gh-pages branch...\n"
cd $HOME
git config --global user.email "travis@travis-ci.org"
git config --global user.name "travis-ci"
git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/$REPO_SLUG.git ghp-repo > /dev/null

echo -e "Updating gh-pages branch...\n"
cd ghp-repo
git rm -rf ./*
cp -Rf $HOME/ghp-staging .
git add -f .
git commit -m "Latest page content on successful travis build $TRAVIS_BUILD_NUMBER auto-pushed to gh-pages"
git push -fq origin gh-pages > /dev/null

echo -e "Published website to gh-pages.\n"
