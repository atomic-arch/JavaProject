@echo off
setlocal

for %%I in ("%~dp0..\..") do set "PROJECT_ROOT=%%~fI"
set "JAVAFX_SDK=%PROJECT_ROOT%\lib\javafx-sdk-21.0.8"
set "JAVAFX_LIB=%JAVAFX_SDK%\lib"
set "SQLITE_JDBC=%PROJECT_ROOT%\lib\sqlite-jdbc-3.53.1.0.jar"

if not exist "%SQLITE_JDBC%" (
    echo SQLite JDBC driver was not found. Run scripts\windows\download-sqlite-jdbc.bat first.
    exit /b 1
)

call "%PROJECT_ROOT%\scripts\windows\build.bat"
if errorlevel 1 exit /b %errorlevel%

java ^
    --sun-misc-unsafe-memory-access=allow ^
    "-Dclinic.database=%PROJECT_ROOT%\data\clinic.db" ^
    --module-path "%JAVAFX_LIB%" ^
    --add-modules javafx.controls ^
    -cp "%PROJECT_ROOT%\build\classes;%SQLITE_JDBC%" ^
    clinic.HelloApplication
