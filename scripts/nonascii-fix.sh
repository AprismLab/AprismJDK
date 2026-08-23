#!/usr/bin/env bash
# Replace non-ASCII glyphs in aprismate-api javadoc/comments with ASCII.
cd /cygdrive/c/Users/Sails/Documents/Workspace/01-Active/Domain-Projects/Aprism/AprismJDK/aprismate-api/src/main/java || exit 1
FILES=$(grep -rlP '[^\x00-\x7F]' . --include=*.java)
echo "$FILES"
for f in $FILES; do
  sed -i 's/→/->/g; s/←/<-/g; s/↓/v/g; s/[µμ]s/us/g; s/✓/yes/g; s/✗/no/g; s/—/--/g; s/–/-/g; s/[─│┌┐└┘├┤┬┴┼]/-/g; s/[“”]/"/g; s/[‘’]/'"'"'/g' "$f"
done
LEFT=$(grep -rlP '[^\x00-\x7F]' . --include=*.java | wc -l)
echo "remaining non-ascii files: $LEFT"
