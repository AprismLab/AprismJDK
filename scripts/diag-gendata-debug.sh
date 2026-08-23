#!/usr/bin/env bash
cd /cygdrive/c/Users/Sails/Documents/Workspace/01-Active/Domain-Projects/Aprism/AprismJDK/openjdk-25 || exit 1
make jdk.compiler-gendata JOBS=2 LOG=debug 2>&1 > /tmp/gendata-debug.log
grep -n 'Creating ct.sym' /tmp/gendata-debug.log | head -2
N=$(grep -n 'Creating ct.sym' /tmp/gendata-debug.log | head -1 | cut -d: -f1)
sed -n "${N},$((N+12))p" /tmp/gendata-debug.log | cut -c1-260
