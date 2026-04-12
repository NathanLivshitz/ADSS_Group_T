#!/bin/bash
set -e
cd "$(dirname "$0")"
rm -rf dev/out
mkdir -p dev/out

echo "Compiling main sources..."
find dev/src/Inventory -name "*.java" > /tmp/sources.txt
if [ -s /tmp/sources.txt ]; then
    javac -d dev/out @/tmp/sources.txt
fi

echo "Compilation successful."
