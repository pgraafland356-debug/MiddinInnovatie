@echo off
REM Middin Innovatie — release checklist (dubbelklik of vanaf projectmap).
cd /d "%~dp0.."
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0release-checklist.ps1" %*
