#!/bin/bash
echo "Publishing build artifacts..."
./gradlew publish

echo "Publishing website..."
./travis/publish-website.sh
