#!/bin/bash

echo "↻ Polling for changes in java files..."

# Compile and run initially
find . -name "*.java" -exec stat --format="%Y %n" {} \; > .timestamps
find . -name "*.java" | xargs javac -cp postgresql.jar && java -cp .:postgresql.jar Main &

while true; do
  sleep 1
  find . -name "*.java" -exec stat --format="%Y %n" {} \; > .timestamps.new

  if ! cmp -s .timestamps .timestamps.new; then
    echo "⚠︎ Detected change in Java files!"
    cp .timestamps.new .timestamps
    # pkill -f "java -cp"
    find . -name "*.java" | xargs javac -cp postgresql.jar && echo "✓ Recompiled GUI"
    java -cp .:postgresql.jar Main &
  fi
done
