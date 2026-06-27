#!/usr/bin/env sh
set -eu

PROJECT_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
JAVAFX_SDK=${JAVAFX_SDK:-"$PROJECT_ROOT/lib/javafx-sdk-21.0.8"}
SQLITE_JDBC=${SQLITE_JDBC:-"$PROJECT_ROOT/lib/sqlite-jdbc-3.53.1.0.jar"}

if [ ! -f "$SQLITE_JDBC" ]; then
    printf 'SQLite JDBC driver was not found. Run ./scripts/linux/download-sqlite-jdbc.sh first.\n' >&2
    exit 1
fi

"$PROJECT_ROOT/scripts/linux/build.sh"

exec java \
    -Dclinic.database="$PROJECT_ROOT/data/clinic.db" \
    --module-path "$JAVAFX_SDK/lib" \
    --add-modules javafx.controls \
    -cp "$PROJECT_ROOT/build/classes:$SQLITE_JDBC" \
    clinic.HelloApplication
