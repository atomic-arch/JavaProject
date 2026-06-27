@echo off
setlocal

for %%I in ("%~dp0..") do set "PROJECT_ROOT=%%~fI"
set "JAVAFX_SDK=%PROJECT_ROOT%\lib\javafx-sdk-21.0.8"
set "SQLITE_JDBC=%PROJECT_ROOT%\lib\sqlite-jdbc-3.53.1.0.jar"

echo ==========================================
echo  MediCare Clinic - Windows Setup Wizard
echo ==========================================
echo.

echo Step 1 of 5: Checking Java...
where java >nul 2>nul
if errorlevel 1 (
    echo Java was not found on PATH.
    echo Install Java JDK 21, then open a new terminal and run this wizard again.
    exit /b 1
)

where javac >nul 2>nul
if errorlevel 1 (
    echo javac was not found on PATH.
    echo Install the Java JDK, not only the JRE. This project needs javac to compile.
    exit /b 1
)

for /f "tokens=*" %%V in ('javac -version 2^>^&1') do set "JAVAC_VERSION=%%V"
echo Found %JAVAC_VERSION%
echo.

echo Step 2 of 5: Checking JavaFX SDK...
if exist "%JAVAFX_SDK%\lib\libglass.so" (
    echo A Linux JavaFX SDK was found in:
    echo %JAVAFX_SDK%
    echo.
    echo Windows needs a JavaFX SDK that contains .dll files instead of Linux .so files.
    choice /c YN /m "Delete the Linux JavaFX SDK folder and download the Windows one"
    if errorlevel 2 (
        echo Setup stopped. Delete lib\javafx-sdk-21.0.8 manually or rerun this wizard and choose Y.
        exit /b 1
    )
    rmdir /s /q "%JAVAFX_SDK%"
)

if not exist "%JAVAFX_SDK%\lib\javafx.controls.jar" (
    call "%PROJECT_ROOT%\scripts\download-javafx-windows.bat"
    if errorlevel 1 exit /b %errorlevel%
) else (
    echo JavaFX SDK is already available at %JAVAFX_SDK%
)
echo.

echo Step 3 of 5: Checking SQLite JDBC...
if not exist "%SQLITE_JDBC%" (
    call "%PROJECT_ROOT%\scripts\download-sqlite-jdbc.bat"
    if errorlevel 1 exit /b %errorlevel%
) else (
    echo SQLite JDBC driver is already available at %SQLITE_JDBC%
)
echo.

echo Step 4 of 5: Building and testing the project...
call "%PROJECT_ROOT%\scripts\test-database.bat"
if errorlevel 1 (
    echo Setup failed during build or database testing.
    exit /b %errorlevel%
)
echo.

echo Step 5 of 5: Setup complete.
echo You can run the app later with:
echo scripts\run.bat
echo.

choice /c YN /m "Launch the app now"
if errorlevel 2 (
    echo Done.
    exit /b 0
)

call "%PROJECT_ROOT%\scripts\run.bat"
