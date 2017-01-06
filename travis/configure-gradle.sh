#!/bin/bash
set -ev
GRADLE_PROPERTIES="$HOME/.gradle/gradle.properties"
echo "Gradle Properties should exist at $GRADLE_PROPERTIES"

if [ ! -f "$GRADLE_PROPERTIES" ]; then
    echo "Gradle Properties does not exist"

    echo "Creating Gradle Properties file..."
    touch $GRADLE_PROPERTIES
    
    echo "Writing ARTIFACTORY_USER to gradle.properties..."
    echo "artifactoryUser=$ARTIFACTORY_USER" >> $GRADLE_PROPERTIES
    
    echo "Writing ARTIFACTORY_PASSWORD to gradle.properties..."
    echo "artifactoryPassword=$ARTIFACTORY_PASSWORD" >> $GRADLE_PROPERTIES
    
    echo "Writing ARTIFACTORY_CONTEXTURL to gradle.properties..."
    echo "artifactoryContextUrl=$ARTIFACTORY_CONTEXTURL" >> $GRADLE_PROPERTIES
    
    echo "Writing BINTRAY_USER to gradle.properties..."
    echo "bintrayUser=$BINTRAY_USER" >> $GRADLE_PROPERTIES
    
    echo "Writing BINTRAY_KEY to gradle.properties..."
    echo "bintrayKey=$BINTRAY_KEY" >> $GRADLE_PROPERTIES
    
    echo "Writing OSSRH_USER to gradle.properties..."
    echo "ossrhUser=$OSSRH_USER" >> $GRADLE_PROPERTIES
    
    echo "Writing OSSRH_PASSWORD to gradle.properties..."
    echo "ossrhPassword=$OSSRH_PASSWORD" >> $GRADLE_PROPERTIES
    
    echo "Writing GH_TOKEN to gradle.properties..."
    echo "ghToken=$GH_TOKEN" >> $GRADLE_PROPERTIES
fi
