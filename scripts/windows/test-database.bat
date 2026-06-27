@echo off
setlocal

for %%I in ("%~dp0..\..") do set "PROJECT_ROOT=%%~fI"
set "SQLITE_JDBC=%PROJECT_ROOT%\lib\sqlite-jdbc-3.53.1.0.jar"
set "TEST_DATABASE=%TEMP%\clinic-data-test.db"

if not exist "%SQLITE_JDBC%" (
    echo SQLite JDBC driver was not found. Run scripts\windows\download-sqlite-jdbc.bat first.
    exit /b 1
)

call "%PROJECT_ROOT%\scripts\windows\build.bat"
if errorlevel 1 exit /b %errorlevel%

if exist "%PROJECT_ROOT%\build\test-classes" rmdir /s /q "%PROJECT_ROOT%\build\test-classes"
mkdir "%PROJECT_ROOT%\build\test-classes"

javac ^
    -cp "%PROJECT_ROOT%\build\classes;%SQLITE_JDBC%" ^
    -d "%PROJECT_ROOT%\build\test-classes" ^
    "%PROJECT_ROOT%\src\test\java\clinic\ClinicDataTest.java"
if errorlevel 1 exit /b %errorlevel%

java ^
    "-Dclinic.database=%TEST_DATABASE%" ^
    -cp "%PROJECT_ROOT%\build\classes;%PROJECT_ROOT%\build\test-classes;%SQLITE_JDBC%" ^
    clinic.ClinicDataTest
