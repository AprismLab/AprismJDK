#!/usr/bin/env bash
LOG=/cygdrive/c/Users/Sails/AppData/Local/Temp/ojdk-build.log
grep -a -iE 'fatal|cannot|failed' "$LOG" | grep -viE 'failure-logs|No indication' | head -10 | cut -c1-190
echo '=== failure-logs dir ==='
ls /cygdrive/c/Users/Sails/Documents/Workspace/01-Active/Domain-Projects/Aprism/AprismJDK/openjdk-25/build/windows-x86_64-server-release/make-support/failure-logs/ 2>/dev/null | head -6
