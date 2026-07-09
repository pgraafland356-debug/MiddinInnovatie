@echo off
title Middin Innovatie - GitHub publiceren
cd /d "%~dp0.."
echo.
echo === Stap 1: Inloggen op GitHub ===
echo Volg de instructies in dit venster (browser opent).
echo.
"%ProgramFiles%\GitHub CLI\gh.exe" auth login --hostname github.com --web --git-protocol https
if errorlevel 1 (
    echo.
    echo Inloggen mislukt. Probeer opnieuw: gh auth login
    pause
    exit /b 1
)
echo.
echo === Stap 2: Repo + release publiceren ===
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup-github-release.ps1"
echo.
pause
