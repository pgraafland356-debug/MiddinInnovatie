@echo off
setlocal
set "DIR=%~dp0"
set "JAR=%DIR%app\MiddinInnovatie.jar"
set "JAVA="

if exist "%DIR%runtime\bin\javaw.exe" set "JAVA=%DIR%runtime\bin\javaw.exe"
if not defined JAVA if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javaw.exe" set "JAVA=%JAVA_HOME%\bin\javaw.exe"
if not defined JAVA if exist "C:\Program Files\Microsoft\jdk-17.0.17.10-hotspot\bin\javaw.exe" set "JAVA=C:\Program Files\Microsoft\jdk-17.0.17.10-hotspot\bin\javaw.exe"
if not defined JAVA for /f "delims=" %%i in ('where javaw 2^>nul') do set "JAVA=%%i" & goto :found
:found
if not defined JAVA (
    echo Java niet gevonden. Herinstalleer Middin Innovatie of installeer JDK 17+.
    pause
    exit /b 1
)
start "" "%JAVA%" -jar "%JAR%"
