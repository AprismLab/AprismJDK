#!/usr/bin/env bash
cd /cygdrive/c/Users/Sails/Documents/Workspace/01-Active/Domain-Projects/Aprism/AprismJDK/aprismate-api/src/main/java || exit 1
for f in jdk/aprismate/config/ConfigException.java \
         jdk/aprismate/reflection/ReflectionException.java \
         jdk/aprismate/serialization/SerializationException.java \
         jdk/aprismate/serialization/impl/SimpleSerializerRegistry.java \
         jdk/aprismate/serialization/impl/StringSerializer.java; do
  sed -i '1s/^\xEF\xBB\xBF//' "$f"
  head -c 3 "$f" | od -An -tx1 | tr -d ' \n'
  echo " <- $f"
done
