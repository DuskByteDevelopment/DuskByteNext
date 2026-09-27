#!/usr/bin/env bash
# Re-inserts META-INF/jars/* extracted by zkm_strip_nested.ps1 back into the jar
# after obfuscation. Usage: zkm_restore_nested.sh /abs/path/to/mod.jar [nested_dir]
set -e
JAR="$(readlink -f "$1")"
NESTED="${2:-$JAR.nested}"
if [ ! -d "$NESTED" ]; then
    # also try relative to cwd (CI layout: zkm_nested/)
    if [ -d "zkm_nested" ]; then NESTED="$(readlink -f zkm_nested)"; else
        echo "  no nested jars dir found, nothing to restore"; exit 0
    fi
fi
cd "$NESTED"
zip -q -r "$JAR" META-INF
cd - > /dev/null
echo "  nested jars restored into $(basename "$JAR")"
