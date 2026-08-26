#!/usr/bin/env bash
LOG=/cygdrive/c/Users/Sails/AppData/Local/Temp/ojdk-build.log
grep -an 'error' "$LOG" | grep -v 'Error 2$' | grep -vi 'ERROR: Build' | tail -12 | cut -c1-200
echo "=== failure log ==="
F=/cygdrive/c/Users/Sails/Documents/Workspace/01-Active/Domain-Projects/Aprism/AprismJDK/openjdk-25/build/windows-x86_64-server-release/make-support/failure-logs/jdk_modules_jdk.aprismate__the.jdk.aprismate_batch.log
if [ -f "$F" ]; then head -20 "$F" | cut -c1-200; else echo "(no failure log)"; fi
