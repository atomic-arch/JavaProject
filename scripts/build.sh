#!/usr/bin/env sh
set -eu

PROJECT_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
JAVAFX_SDK=${JAVAFX_SDK:-"$PROJECT_ROOT/lib/javafx-sdk-21.0.8"}
JAVAFX_LIB="$JAVAFX_SDK/lib"

if [ ! -d "$JAVAFX_LIB" ]; then
    printf 'JavaFX SDK was not found. Run ./scripts/download-javafx.sh first.\n' >&2
    exit 1
fi

rm -rf "$PROJECT_ROOT/build/classes"
mkdir -p "$PROJECT_ROOT/build/classes"
find "$PROJECT_ROOT/src/main/java" -name '*.java' -print0 |
    xargs -0 javac \
        --module-path "$JAVAFX_LIB" \
        --add-modules javafx.controls \
        -d "$PROJECT_ROOT/build/classes"

printf 'Compiled classes are available in %s\n' "$PROJECT_ROOT/build/classes"
