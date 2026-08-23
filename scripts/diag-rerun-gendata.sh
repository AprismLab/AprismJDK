#!/usr/bin/env bash
cd /cygdrive/c/Users/Sails/Documents/Workspace/01-Active/Domain-Projects/Aprism/AprismJDK/openjdk-25 || exit 1
make jdk.compiler-gendata JOBS=4 LOG=info 2>&1 | grep -v "^Copying" | tail -30
echo "TARGET_EXIT=$?"
