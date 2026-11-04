@echo off
setlocal
cd /d "%~dp0"
title Battleship - Console Edition

where java >nul 2>&1
if %ERRORLEVEL% equ 0 (
    java -jar "%~dp0lib\battleship-1.0.0.jar" --console %*
    pause
    exit /b 0
)

echo [ERROR] Java 21+ is required to run Battleship.
echo Download at https://adoptium.net
pause
exit /b 1
