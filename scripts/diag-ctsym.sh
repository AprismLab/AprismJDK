#!/usr/bin/env bash
LOG=/cygdrive/c/Users/Sails/AppData/Local/Temp/ojdk-build.log
grep -an 'ct.sym' "$LOG" | tail -4 | cut -c1-200
echo '=== context around failure ==='
N=$(grep -an 'Gendata' "$LOG" | head -1 | cut -d: -f1)
[ -n "$N" ] && sed -n "$((N-20)),$((N+2))p" "$LOG" | cut -c1-190
