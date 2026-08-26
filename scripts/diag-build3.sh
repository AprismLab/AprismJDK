#!/usr/bin/env bash
LOG=/cygdrive/c/Users/Sails/AppData/Local/Temp/ojdk-build.log
echo "=== all unique error lines ==="
grep -a 'error' "$LOG" | grep -v 'Error 2$' | grep -vi 'ERROR: Build' | sort -u | head -15 | cut -c1-220
echo "=== failure logs ==="
FL=/cygdrive/c/Users/Sails/Documents/Workspace/01-Active/Domain-Projects/Aprism/AprismJDK/openjdk-25/build/windows-x86_64-server-release/make-support/failure-logs
ls "$FL" 2>/dev/null | head -5
for f in "$FL"/*.log; do
  [ -f "$f" ] || continue
  echo "== $(basename $f)"
  head -8 "$f" | cut -c1-220
done
