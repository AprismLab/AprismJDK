#!/usr/bin/env bash
# De-glyph ALL Java sources under both source and overlay trees.
BASE=/cygdrive/c/Users/Sails/Documents/Workspace/01-Active/Domain-Projects/Aprism/AprismJDK
for dir in "$BASE/aprismate-api/src/main/java" "$BASE/jdk/overlay/src/jdk.aprismate/share/classes"; do
  cd "$dir" || continue
  FILES=$(grep -rlP '[^\x00-\x7F]' . --include='*.java' 2>/dev/null)
  if [ -z "$FILES" ]; then
    echo "CLEAN: $dir"
    continue
  fi
  echo "$FILES" | while IFS= read -r f; do
    sed -i \
      's/\xE2\x86\x92/->/g; s/\xE2\x86\x90/<-/g; s/\xE2\x86\x93/v/g; s/\xC2\xB5us/us/g; s/\xCE\xBCs/us/g; s/\xE2\x9C\x93/yes/g; s/\xE2\x9C\x97/no/g; s/\xE2\x80\x94/--/g; s/\xE2\x80\x93/-/g; s/\xE2\x94\x80/-/g; s/\xE2\x94\x82/|/g; s/\xE2\x80\xA6/.../g; s/\xC2\xA0/ /g' \
      "$f"
  done
  LEFT=$(grep -rlP '[^\x00-\x7F]' . --include='*.java' 2>/dev/null | wc -l)
  echo "FIXED: $dir ($LEFT remaining)"
done
