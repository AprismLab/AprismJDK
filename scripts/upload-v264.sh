#!/usr/bin/env bash
cd /cygdrive/c/Users/Sails/Documents/Workspace/01-Active/Domain-Projects/Aprism/AprismJDK/releases || exit 1
for f in "AprismJDK-26.4-windows-x64-jdk.zip" "AprismJDK-26.4-windows-x64-jdk.tar.gz"; do
  for attempt in 1 2 3 4 5; do
    echo "== upload $f (attempt $attempt)"
    if gh release upload v26.4 "$f" --clobber; then
      echo "UPLOADED: $f"
      break
    fi
    echo "retry in 30s..."
    sleep 30
  done
done
echo "=== final ==="
gh release view v26.4 --json isDraft,isPrerelease,assets --jq '{isDraft, isPrerelease, assets: [.assets[].name]}'
