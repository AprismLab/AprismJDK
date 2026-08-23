#!/usr/bin/env bash
# Full images build with transient-failure retry (Cygwin spawn flakiness).
# Each retry is incremental; convergence typically within 1-3 attempts.
cd /cygdrive/c/Users/Sails/Documents/Workspace/01-Active/Domain-Projects/Aprism/AprismJDK/openjdk-25 || exit 9
for attempt in 1 2 3 4; do
  echo "=== ATTEMPT $attempt ==="
  make images JOBS=4 LOG=info
  rc=$?
  echo "ATTEMPT${attempt}_RC=$rc"
  [ $rc -eq 0 ] && break
done
echo "BUILD_EXIT=$rc"
