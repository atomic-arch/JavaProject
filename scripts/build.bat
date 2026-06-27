@echo off
setlocal

for %%I in ("%~dp0..") do set "PROJECT_ROOT=%%~fI"
set "JAVAFX_SDK=%PROJECT_ROOT%\lib\javafx-sdk-21.0.8"
set "JAVAFX_LIB=%JAVAFX_SDK%\lib"
set "SOURCES_FILE=%PROJECT_ROOT%\build\sources.txt"

if not exist "%JAVAFX_LIB%" (
    echo JavaFX SDK was not found. Run scripts\download-javafx-windows.bat first.
    exit /b 1
)

if exist "%JAVAFX_LIB%\libglass.so" (
    echo The JavaFX SDK in lib\javafx-sdk-21.0.8 is the Linux version.
    echo Delete lib\javafx-sdk-21.0.8 and run scripts\download-javafx-windows.bat.
    exit /b 1
)

if exist "%PROJECT_ROOT%\build\classes" rmdir /s /q "%PROJECT_ROOT%\build\classes"
if not exist "%PROJECT_ROOT%\build\classes" mkdir "%PROJECT_ROOT%\build\classes"

if exist "%SOURCES_FILE%" del /q "%SOURCES_FILE%"
powershell -NoProfile -ExecutionPolicy Bypass -Command "Get-ChildItem -Path '%PROJECT_ROOT%\src\main\java' -Recurse -Filter *.java | ForEach-Object { '\"' + ($_.FullName -replace '\\', '/') + '\"' } | Set-Content -Path '%SOURCES_FILE%' -Encoding ASCII"
if errorlevel 1 exit /b %errorlevel%

javac ^
    --module-path "%JAVAFX_LIB%" ^
    --add-modules javafx.controls ^
    -d "%PROJECT_ROOT%\build\classes" ^
    @"%SOURCES_FILE%"
if errorlevel 1 exit /b %errorlevel%

echo Compiled classes are available in %PROJECT_ROOT%\build\classes
