#!/usr/bin/env bash
# Report non-ASCII occurrences in aprismate-api sources (context preview).
cd /cygdrive/c/Users/Sails/Documents/Workspace/01-Active/Domain-Projects/Aprism/AprismJDK/aprismate-api/src/main/java || exit 1
grep -rlP '[^\x00-\x7F]' . --include=*.java > /tmp/nonascii.txt
echo "files: $(wc -l < /tmp/nonascii.txt)"
while IFS= read -r f; do
  echo "== $f"
  grep -nP '[^\x00-\x7F]' "$f" | head -2 | cut -c1-140
done < /tmp/nonascii.txt | head -60
