#!/usr/bin/env bash
cd /cygdrive/c/Users/Sails/Documents/Workspace/01-Active/Domain-Projects/Aprism/AprismJDK/aprismate-api/src/main/java || exit 1
echo "--- offending chars per remaining file ---"
for f in $(grep -rlP '[^\x00-\x7F]' . --include='*.java'); do
  echo "== $f"
  grep -noP '[^\x00-\x7F]' "$f" | sort | uniq -c | sort -rn | head -5
done
