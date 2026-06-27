#!/usr/bin/env sh
set -eu

PROJECT_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
SQLITE_JDBC=${SQLITE_JDBC:-"$PROJECT_ROOT/lib/sqlite-jdbc-3.53.1.0.jar"}
TEST_DATABASE=/tmp/clinic-data-test.db

if [ ! -f "$SQLITE_JDBC" ]; then
    printf 'SQLite JDBC driver was not found. Run ./scripts/linux/download-sqlite-jdbc.sh first.\n' >&2
    exit 1
fi

"$PROJECT_ROOT/scripts/linux/build.sh"
mkdir -p "$PROJECT_ROOT/build/test-classes"

javac \
    -cp "$PROJECT_ROOT/build/classes:$SQLITE_JDBC" \
    -d "$PROJECT_ROOT/build/test-classes" \
    "$PROJECT_ROOT/src/test/java/clinic/ClinicDataTest.java"

exec java \
    -Dclinic.database="$TEST_DATABASE" \
    -cp "$PROJECT_ROOT/build/classes:$PROJECT_ROOT/build/test-classes:$SQLITE_JDBC" \
    clinic.ClinicDataTest
