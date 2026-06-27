@echo off
setlocal

for %%I in ("%~dp0..\..") do set "PROJECT_ROOT=%%~fI"
set "APP_NAME=MediCare Clinic"
if not defined APP_VERSION set "APP_VERSION=1.0.1"
set "APP_JAR=MediCareClinic.jar"
set "JAVAFX_SDK=%PROJECT_ROOT%\lib\javafx-sdk-21.0.8"
set "JAVAFX_LIB=%JAVAFX_SDK%\lib"
set "SQLITE_JDBC=%PROJECT_ROOT%\lib\sqlite-jdbc-3.53.1.0.jar"
set "DIST_DIR=%PROJECT_ROOT%\dist"
set "INPUT_DIR=%DIST_DIR%\package-input"
set "APP_IMAGE_PARENT=%DIST_DIR%\app-image"
set "APP_IMAGE_DIR=%APP_IMAGE_PARENT%\%APP_NAME%"
set "OUTPUT_DIR=%DIST_DIR%\windows"
set "MANIFEST_FILE=%DIST_DIR%\manifest.txt"
set "WIX_DIR=%PROJECT_ROOT%\tools\wix\wix314"
set "WIX_ARCHIVE=%PROJECT_ROOT%\tools\wix\wix314-binaries.zip"
set "WIX_URL=https://github.com/wixtoolset/wix3/releases/download/wix3141rtm/wix314-binaries.zip"
set "JPACKAGE_EXE="
set "JAR_EXE="

for %%T in (jpackage.exe) do set "JPACKAGE_EXE=%%~$PATH:T"
if not defined JPACKAGE_EXE if defined JAVA_HOME if exist "%JAVA_HOME%\bin\jpackage.exe" set "JPACKAGE_EXE=%JAVA_HOME%\bin\jpackage.exe"
if not defined JPACKAGE_EXE for /d %%D in ("%ProgramFiles%\Java\jdk-*") do if exist "%%~fD\bin\jpackage.exe" set "JPACKAGE_EXE=%%~fD\bin\jpackage.exe"
if not defined JPACKAGE_EXE for /d %%D in ("%ProgramFiles%\Eclipse Adoptium\jdk-*") do if exist "%%~fD\bin\jpackage.exe" set "JPACKAGE_EXE=%%~fD\bin\jpackage.exe"
if not defined JPACKAGE_EXE (
    echo jpackage was not found on PATH.
    echo Install a full Java JDK that includes jpackage, then open a new terminal and try again.
    exit /b 1
)

for %%T in (jar.exe) do set "JAR_EXE=%%~$PATH:T"
if not defined JAR_EXE for %%I in ("%JPACKAGE_EXE%\..") do if exist "%%~fI\jar.exe" set "JAR_EXE=%%~fI\jar.exe"
if not defined JAR_EXE if defined JAVA_HOME if exist "%JAVA_HOME%\bin\jar.exe" set "JAR_EXE=%JAVA_HOME%\bin\jar.exe"
if not defined JAR_EXE (
    echo jar was not found. Install a full Java JDK, then open a new terminal and try again.
    exit /b 1
)

where candle >nul 2>nul
if errorlevel 1 (
    if not exist "%WIX_DIR%\candle.exe" (
        echo WiX was not found. Downloading portable WiX tools...
        if not exist "%PROJECT_ROOT%\tools\wix" mkdir "%PROJECT_ROOT%\tools\wix"
        powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%WIX_URL%' -OutFile '%WIX_ARCHIVE%'"
        if errorlevel 1 exit /b %errorlevel%
        powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -LiteralPath '%WIX_ARCHIVE%' -DestinationPath '%WIX_DIR%' -Force"
        if errorlevel 1 exit /b %errorlevel%
    )
    set "PATH=%WIX_DIR%;%PATH%"
)

if not exist "%SQLITE_JDBC%" (
    echo SQLite JDBC driver was not found. Run scripts\windows\download-sqlite-jdbc.bat first.
    exit /b 1
)

if not exist "%JAVAFX_LIB%\javafx.controls.jar" (
    echo JavaFX SDK was not found. Run scripts\windows\download-javafx-windows.bat first.
    exit /b 1
)

echo Using jpackage: %JPACKAGE_EXE%

call "%PROJECT_ROOT%\scripts\windows\build.bat"
if errorlevel 1 exit /b %errorlevel%

if exist "%DIST_DIR%" rmdir /s /q "%DIST_DIR%"
mkdir "%INPUT_DIR%"
mkdir "%OUTPUT_DIR%"

(
    echo Main-Class: clinic.HelloApplication
    echo Class-Path: sqlite-jdbc-3.53.1.0.jar
) > "%MANIFEST_FILE%"

"%JAR_EXE%" --create --file "%INPUT_DIR%\%APP_JAR%" --manifest "%MANIFEST_FILE%" -C "%PROJECT_ROOT%\build\classes" .
if errorlevel 1 exit /b %errorlevel%

copy "%SQLITE_JDBC%" "%INPUT_DIR%\" >nul
if errorlevel 1 exit /b %errorlevel%

"%JPACKAGE_EXE%" ^
    --type app-image ^
    --name "%APP_NAME%" ^
    --app-version "%APP_VERSION%" ^
    --input "%INPUT_DIR%" ^
    --main-jar "%APP_JAR%" ^
    --module-path "%JAVAFX_LIB%" ^
    --add-modules javafx.controls,java.sql ^
    --dest "%APP_IMAGE_PARENT%"
if errorlevel 1 exit /b %errorlevel%

copy "%JAVAFX_SDK%\bin\*.dll" "%APP_IMAGE_DIR%\runtime\bin\" >nul
if errorlevel 1 exit /b %errorlevel%

if exist "%OUTPUT_DIR%" rmdir /s /q "%OUTPUT_DIR%"
mkdir "%OUTPUT_DIR%"

powershell -NoProfile -ExecutionPolicy Bypass -Command "Compress-Archive -LiteralPath '%APP_IMAGE_DIR%' -DestinationPath '%OUTPUT_DIR%\MediCareClinic-%APP_VERSION%-portable.zip' -Force"
if errorlevel 1 exit /b %errorlevel%

"%JPACKAGE_EXE%" ^
    --type exe ^
    --app-image "%APP_IMAGE_DIR%" ^
    --dest "%OUTPUT_DIR%" ^
    --win-per-user-install ^
    --win-menu ^
    --win-shortcut
if errorlevel 1 exit /b %errorlevel%

echo Windows installer and portable zip created in %OUTPUT_DIR%
