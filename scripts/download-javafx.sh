#!/usr/bin/env sh
set -eu

PROJECT_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
JAVAFX_VERSION=21.0.8
JAVAFX_SDK="$PROJECT_ROOT/lib/javafx-sdk-$JAVAFX_VERSION"
JAVAFX_URL="https://download2.gluonhq.com/openjfx/$JAVAFX_VERSION/openjfx-${JAVAFX_VERSION}_linux-x64_bin-sdk.zip"

if [ -d "$JAVAFX_SDK/lib" ]; then
    printf 'JavaFX SDK is already available at %s\n' "$JAVAFX_SDK"
    exit 0
fi

mkdir -p "$PROJECT_ROOT/lib"
ARCHIVE=$(mktemp)
trap 'rm -f "$ARCHIVE"' EXIT

printf 'Downloading JavaFX SDK %s...\n' "$JAVAFX_VERSION"
curl -fL "$JAVAFX_URL" -o "$ARCHIVE"
unzip -q "$ARCHIVE" -d "$PROJECT_ROOT/lib"
printf 'JavaFX SDK installed at %s\n' "$JAVAFX_SDK"
