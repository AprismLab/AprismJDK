#!/usr/bin/env bash
# Publish v26.2 GA release: upload big assets with retries, then flip draft.
set -u
cd /cygdrive/c/Users/Sails/Documents/Workspace/01-Active/Domain-Projects/Aprism/AprismJDK/releases || exit 1

upload() {
  local f="$1"
  for i in 1 2 3 4 5 6; do
    echo "--- upload attempt $i: $f"
    if gh release upload v26.2 "$f" --clobber; then
      echo "UPLOADED: $f"
      return 0
    fi
    sleep $((i * 20))
  done
  echo "FAILED: $f"
  return 1
}

ok=1
upload "AprismJDK-26.2-windows-x64-jdk.zip" || ok=0
upload "AprismJDK-26.2-windows-x64-jdk.tar.gz" || ok=0

if [ $ok -eq 1 ]; then
  gh release edit v26.2 --draft=false --latest
  echo "PUBLISHED"
else
  echo "PUBLISH_ABORTED_ASSETS_MISSING"
fi
gh release view v26.2 --json isDraft,assets --jq '{draft:.isDraft,n:(.assets|length),names:[.assets[].name]}'
