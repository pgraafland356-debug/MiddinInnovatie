@echo off
REM Middin Innovatie — controleer en installeer updates (stille modus voor Taakplanner).
REM Dubbelklik voor het update-venster, of plan dit script in met /silent.

set "UPDATER=%~dp0MiddinInnovatie-Update.exe"
if not exist "%UPDATER%" (
    set "UPDATER=%LOCALAPPDATA%\Programs\MiddinInnovatie\MiddinInnovatie-Update.exe"
)
if not exist "%UPDATER%" (
    echo MiddinInnovatie-Update.exe niet gevonden.
    echo Installeer Middin Innovatie opnieuw of kopieer de updater naast dit script.
    pause
    exit /b 2
)
start "" "%UPDATER%" %*
