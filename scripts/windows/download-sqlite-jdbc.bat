@echo off
setlocal

for %%I in ("%~dp0..\..") do set "PROJECT_ROOT=%%~fI"
set "SQLITE_JDBC_VERSION=3.53.1.0"
set "SQLITE_JDBC=%PROJECT_ROOT%\lib\sqlite-jdbc-%SQLITE_JDBC_VERSION%.jar"
set "SQLITE_JDBC_URL=https://repo.maven.apache.org/maven2/org/xerial/sqlite-jdbc/%SQLITE_JDBC_VERSION%/sqlite-jdbc-%SQLITE_JDBC_VERSION%.jar"

if exist "%SQLITE_JDBC%" (
    echo SQLite JDBC driver is already available at %SQLITE_JDBC%
    exit /b 0
)

if not exist "%PROJECT_ROOT%\lib" mkdir "%PROJECT_ROOT%\lib"

echo Downloading SQLite JDBC driver %SQLITE_JDBC_VERSION%...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%SQLITE_JDBC_URL%' -OutFile '%SQLITE_JDBC%'"
if errorlevel 1 exit /b %errorlevel%

echo SQLite JDBC driver installed at %SQLITE_JDBC%
