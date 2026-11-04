@echo off
setlocal
cd /d "%~dp0"

where javaw >nul 2>&1
if %ERRORLEVEL% equ 0 (
    start "" javaw -jar "%~dp0lib\fleetstrike-1.0.0.jar" %*
    exit /b 0
)

where java >nul 2>&1
if %ERRORLEVEL% equ 0 (
    start "" java -jar "%~dp0lib\fleetstrike-1.0.0.jar" %*
    exit /b 0
)

echo ========================================================
echo  FLEET STRIKE - Java 21+ Required
echo ========================================================
echo.
echo Java runtime was not found on your system PATH.
echo Fleet Strike requires Java 21 or newer to run.
echo.
echo Please download and install Java from:
echo   https://adoptium.net/temurin/releases/?version=21
echo.
echo ========================================================
pause
exit /b 1
