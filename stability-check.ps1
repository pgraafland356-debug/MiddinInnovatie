# Middin Innovatie — local stability gate (same order as CI + optional device tests).
# Run from this folder:  .\stability-check.ps1
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

Write-Host "== assembleDebug + unit tests ==" -ForegroundColor Cyan
.\gradlew.bat assembleDebug testDebugUnitTest --no-daemon
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$adb = Get-Command adb -ErrorAction SilentlyContinue
if ($adb) {
    $devices = & adb devices 2>$null | Select-String "device$" | Where-Object { $_ -notmatch "List of devices" }
    if ($devices) {
        Write-Host "== connectedAndroidTest (device/emulator detected) ==" -ForegroundColor Cyan
        .\gradlew.bat connectedDebugAndroidTest --no-daemon
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    } else {
        Write-Host "No adb device: skipped connectedAndroidTest. Start an emulator or plug in a device to run it." -ForegroundColor Yellow
    }
} else {
    Write-Host "adb not on PATH: skipped connectedAndroidTest." -ForegroundColor Yellow
}

Write-Host "Done." -ForegroundColor Green
