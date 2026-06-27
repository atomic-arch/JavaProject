#!/usr/bin/env sh
set -eu

PROJECT_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
SQLITE_JDBC_VERSION=3.53.1.0
SQLITE_JDBC="$PROJECT_ROOT/lib/sqlite-jdbc-$SQLITE_JDBC_VERSION.jar"
SQLITE_JDBC_URL="https://repo.maven.apache.org/maven2/org/xerial/sqlite-jdbc/$SQLITE_JDBC_VERSION/sqlite-jdbc-$SQLITE_JDBC_VERSION.jar"

if [ -f "$SQLITE_JDBC" ]; then
    printf 'SQLite JDBC driver is already available at %s\n' "$SQLITE_JDBC"
    exit 0
fi

mkdir -p "$PROJECT_ROOT/lib"
printf 'Downloading SQLite JDBC driver %s...\n' "$SQLITE_JDBC_VERSION"
curl -fL "$SQLITE_JDBC_URL" -o "$SQLITE_JDBC"
printf 'SQLite JDBC driver installed at %s\n' "$SQLITE_JDBC"
