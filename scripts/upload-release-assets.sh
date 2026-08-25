#!/usr/bin/env bash
cd /cygdrive/c/Users/Sails/Documents/Workspace/01-Active/Domain-Projects/Aprism/AprismJDK/releases || exit 1
for f in "AprismJDK-26.2-windows-x64-jdk.zip" "AprismJDK-26.2-windows-x64-jdk.tar.gz"; do
  for attempt in 1 2 3 4; do
    echo "== upload $f (attempt $attempt)"
    if gh release upload v26.2 "$f" --clobber; then
      echo "UPLOADED: $f"
      break
    fi
    sleep 20
  done
done
gh release view v26.2 --json assets --jq '.assets[].name'
