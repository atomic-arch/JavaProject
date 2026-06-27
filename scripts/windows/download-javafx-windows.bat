@echo off
setlocal

for %%I in ("%~dp0..\..") do set "PROJECT_ROOT=%%~fI"
set "JAVAFX_VERSION=21.0.8"
set "JAVAFX_SDK=%PROJECT_ROOT%\lib\javafx-sdk-%JAVAFX_VERSION%"
set "JAVAFX_URL=https://download2.gluonhq.com/openjfx/%JAVAFX_VERSION%/openjfx-%JAVAFX_VERSION%_windows-x64_bin-sdk.zip"
set "ARCHIVE=%TEMP%\openjfx-%JAVAFX_VERSION%_windows-x64_bin-sdk.zip"

if exist "%JAVAFX_SDK%\lib\libglass.so" (
    echo A Linux JavaFX SDK was found at %JAVAFX_SDK%.
    echo Delete that folder, then run this script again to install the Windows JavaFX SDK.
    exit /b 1
)

if exist "%JAVAFX_SDK%\lib\javafx.controls.jar" (
    echo JavaFX SDK is already available at %JAVAFX_SDK%
    exit /b 0
)

if not exist "%PROJECT_ROOT%\lib" mkdir "%PROJECT_ROOT%\lib"

echo Downloading JavaFX SDK %JAVAFX_VERSION% for Windows...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%JAVAFX_URL%' -OutFile '%ARCHIVE%'"
if errorlevel 1 exit /b %errorlevel%

echo Extracting JavaFX SDK...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -LiteralPath '%ARCHIVE%' -DestinationPath '%PROJECT_ROOT%\lib' -Force"
if errorlevel 1 exit /b %errorlevel%

del "%ARCHIVE%" >nul 2>nul
echo JavaFX SDK installed at %JAVAFX_SDK%
