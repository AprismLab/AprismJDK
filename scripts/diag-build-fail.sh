#!/usr/bin/env bash
LOG=/cygdrive/c/Users/Sails/AppData/Local/Temp/ojdk-build.log
echo "=== last errors ==="
grep -an 'Error\|error:' "$LOG" | grep -v 'Error 2$' | grep -v 'ERROR: Build' | tail -8 | cut -c1-200
echo "=== failure-logs ==="
ls /cygdrive/c/Users/Sails/Documents/Workspace/01-Active/Domain-Projects/Aprism/AprismJDK/openjdk-25/build/windows-x86_64-server-release/make-support/failure-logs/ 2>/dev/null | head -5
for f in /cygdrive/c/Users/Sails/Documents/Workspace/01-Active/Domain-Projects/Aprism/AprismJDK/openjdk-25/build/windows-x86_64-server-release/make-support/failure-logs/*.log; do
  echo "== $f"
  head -10 "$f" | cut -c1-200
done
