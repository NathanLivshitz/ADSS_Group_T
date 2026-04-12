#!/bin/bash
set -e
cd "$(dirname "$0")"

# Compile main sources first
bash compile.sh

JUNIT_JAR=dev/lib/junit-platform-console-standalone.jar

echo "Compiling tests..."
find dev/src/tests -name "*.java" > /tmp/test_sources.txt
if [ -s /tmp/test_sources.txt ]; then
    javac -d dev/out -cp "dev/out:$JUNIT_JAR" @/tmp/test_sources.txt
else
    echo "No test files found."
    exit 0
fi

echo "Running tests..."
java -jar "$JUNIT_JAR" execute --class-path dev/out --scan-class-path
