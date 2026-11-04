@echo off
setlocal
cd /d "%~dp0"
title Fleet Strike - Console Edition

where java >nul 2>&1
if %ERRORLEVEL% equ 0 (
    java -jar "%~dp0lib\fleetstrike-1.0.0.jar" --console %*
    pause
    exit /b 0
)

echo [ERROR] Java 21+ is required to run Fleet Strike.
echo Download at https://adoptium.net
pause
exit /b 1
