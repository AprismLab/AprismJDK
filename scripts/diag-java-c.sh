#!/usr/bin/env bash
LOG=/cygdrive/c/Users/Sails/AppData/Local/Temp/ojdk-build.log
grep -an 'java.c' "$LOG" | grep -iE 'error|warning C' | head -10 | cut -c1-200
echo '=== generic errors ==='
grep -a 'error' "$LOG" | grep -v 'Error 2' | head -8 | cut -c1-200
