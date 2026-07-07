# Silent/command-line install (fallback if Setup.exe unavailable)
$ErrorActionPreference = "Stop"
$installRoot = Join-Path $env:LOCALAPPDATA "Programs\MiddinInnovatie"
$payload = Join-Path $PSScriptRoot "payload"
if (-not (Test-Path $payload)) {
    $payload = Join-Path $PSScriptRoot "app"
}
if (-not (Test-Path $payload)) { throw "Installer corrupt: payload folder missing." }

Write-Host "Installing Middin Innovatie to: $installRoot" -ForegroundColor Cyan
$appDir = Join-Path $installRoot "app"
New-Item -ItemType Directory -Path $appDir -Force | Out-Null

$jarSrc = Join-Path $payload "app\MiddinInnovatie.jar"
if (-not (Test-Path $jarSrc)) { $jarSrc = Join-Path $payload "MiddinInnovatie.jar" }
Copy-Item $jarSrc (Join-Path $appDir "MiddinInnovatie.jar") -Force

$exeSrc = Join-Path $payload "MiddinInnovatie.exe"
if (Test-Path $exeSrc) { Copy-Item $exeSrc (Join-Path $installRoot "MiddinInnovatie.exe") -Force }

$batSrc = Join-Path $payload "Start Middin Innovatie.bat"
if (Test-Path $batSrc) {
    Copy-Item $batSrc (Join-Path $installRoot "Start Middin Innovatie.bat") -Force
} else {
    Copy-Item (Join-Path $PSScriptRoot "Start-MiddinInnovatie.bat") (Join-Path $installRoot "Start Middin Innovatie.bat") -Force -ErrorAction SilentlyContinue
}

$rtSrc = Join-Path $payload "runtime"
if (Test-Path $rtSrc) {
    Write-Host "Installing bundled Java runtime..." -ForegroundColor Cyan
    $rtDest = Join-Path $installRoot "runtime"
    if (Test-Path $rtDest) { Remove-Item $rtDest -Recurse -Force }
    Copy-Item $rtSrc $rtDest -Recurse -Force
} else {
    Write-Host "Warning: no bundled Java in payload. JDK 17+ must be installed on this PC." -ForegroundColor Yellow
}

$target = Join-Path $installRoot "MiddinInnovatie.exe"
if (-not (Test-Path $target)) { $target = Join-Path $installRoot "Start Middin Innovatie.bat" }

$wsh = New-Object -ComObject WScript.Shell
$startMenu = [Environment]::GetFolderPath("Programs")
$shortcutDir = Join-Path $startMenu "Middin Innovatie"
New-Item -ItemType Directory -Path $shortcutDir -Force | Out-Null
$lnk = $wsh.CreateShortcut((Join-Path $shortcutDir "Middin Innovatie.lnk"))
$lnk.TargetPath = $target
$lnk.WorkingDirectory = $installRoot
$lnk.Description = "Middin Innovatie desktop"
$lnk.Save()

$desktop = [Environment]::GetFolderPath("Desktop")
$deskLnk = $wsh.CreateShortcut((Join-Path $desktop "Middin Innovatie.lnk"))
$deskLnk.TargetPath = $target
$deskLnk.WorkingDirectory = $installRoot
$deskLnk.Save()

$uninstallSrc = Join-Path $payload "MiddinInnovatie-Uninstall.exe"
if (-not (Test-Path $uninstallSrc)) {
    $uninstallSrc = Join-Path $PSScriptRoot "MiddinInnovatie-Uninstall.exe"
}
if (Test-Path $uninstallSrc) {
    Copy-Item $uninstallSrc (Join-Path $installRoot "MiddinInnovatie-Uninstall.exe") -Force
    $uninstallExe = Join-Path $installRoot "MiddinInnovatie-Uninstall.exe"
    $unlnk = $wsh.CreateShortcut((Join-Path $shortcutDir "Middin Innovatie verwijderen.lnk"))
    $unlnk.TargetPath = $uninstallExe
    $unlnk.WorkingDirectory = $installRoot
    $unlnk.Description = "Middin Innovatie verwijderen"
    $unlnk.Save()
    $uninstallPs1 = Join-Path $installRoot "uninstall.ps1"
    Set-Content -Path $uninstallPs1 -Value "Start-Process -FilePath '$uninstallExe' -Wait" -Encoding UTF8
}

Write-Host ""
Write-Host "Installation complete." -ForegroundColor Green
$run = Read-Host "Start Middin Innovatie now? (Y/n)"
if ($run -eq '' -or $run -eq 'Y' -or $run -eq 'y') { Start-Process $target }
