# Full Middin Innovatie Windows distribution ZIP (setup, updater, uninstall, portable app).
param(
    [string]$OutZip = ""
)

$ErrorActionPreference = "Stop"
$repo = Split-Path -Parent $PSScriptRoot
. (Join-Path $repo "scripts\read-app-version.ps1")
. (Join-Path $repo "scripts\read-github-update-config.ps1")
$version = (Get-AppVersion -Root $repo).VersionName
$gh = Get-MiddinGithubUpdateConfig -Root $repo

$installerDir = Join-Path $repo "desktop\build\installer"
$portableSrc = Join-Path $repo "desktop\build\dist\MiddinInnovatie"
$setupName = "MiddinInnovatie-Windows-Setup-$version.exe"
$uninstallName = "MiddinInnovatie-Windows-Uninstall-$version.exe"

foreach ($req in @(
    (Join-Path $installerDir $setupName),
    (Join-Path $installerDir $uninstallName),
    (Join-Path $installerDir "MiddinInnovatie-Update.exe"),
    (Join-Path $portableSrc "MiddinInnovatie.exe"),
    (Join-Path $portableSrc "app\MiddinInnovatie.jar"),
    (Join-Path $portableSrc "runtime\bin\javaw.exe")
)) {
    if (-not (Test-Path $req)) {
        Write-Host "Missing $req - run create-windows-installer.ps1 first." -ForegroundColor Yellow
        & (Join-Path $repo "scripts\create-windows-installer.ps1")
        break
    }
}

$stage = Join-Path $env:TEMP ("MiddinInnovatie-dist-" + [guid]::NewGuid().ToString("N"))
New-Item -ItemType Directory -Path $stage -Force | Out-Null
$portableOut = Join-Path $stage "MiddinInnovatie-portable"
New-Item -ItemType Directory -Path $portableOut -Force | Out-Null

Get-Process -Name java,javaw,MiddinInnovatie -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2

Copy-Item (Join-Path $installerDir $setupName) (Join-Path $stage $setupName) -Force
Copy-Item (Join-Path $installerDir $uninstallName) (Join-Path $stage $uninstallName) -Force
Copy-Item (Join-Path $installerDir "MiddinInnovatie-Update.exe") (Join-Path $stage "MiddinInnovatie-Update.exe") -Force
Copy-Item (Join-Path $repo "scripts\installer\Check-MiddinUpdate.bat") (Join-Path $stage "Check-MiddinUpdate.bat") -Force

Copy-Item (Join-Path $portableSrc "MiddinInnovatie.exe") (Join-Path $portableOut "MiddinInnovatie.exe") -Force
Copy-Item (Join-Path $portableSrc "Start Middin Innovatie.bat") (Join-Path $portableOut "Start Middin Innovatie.bat") -Force -ErrorAction SilentlyContinue
Copy-Item (Join-Path $installerDir "MiddinInnovatie-Update.exe") (Join-Path $portableOut "MiddinInnovatie-Update.exe") -Force
Copy-Item (Join-Path $portableSrc "app") (Join-Path $portableOut "app") -Recurse -Force
Copy-Item (Join-Path $portableSrc "runtime") (Join-Path $portableOut "runtime") -Recurse -Force
Copy-Item (Join-Path $installerDir $uninstallName) (Join-Path $portableOut "MiddinInnovatie-Uninstall.exe") -Force

@"
Middin Innovatie v$version - Windows distributie
===============================================

Aanbevolen (nieuwe pc):
  1. Dubbelklik $setupName
  2. Volg de installatiewizard

Updates (al geinstalleerd):
  - MiddinInnovatie-Update.exe
  - of Check-MiddinUpdate.bat

Zonder installatie (portable map):
  - MiddinInnovatie-portable\MiddinInnovatie.exe
  - Java zit in runtime\ (geen aparte installatie nodig)

Verwijderen:
  - $uninstallName
  - of na installatie: Start-menu > Middin Innovatie verwijderen

Auto-update feed: $($gh.FeedUrl)
GitHub releases: $($gh.ReleasesUrl)
"@ | Set-Content (Join-Path $stage "LEESMIJ.txt") -Encoding UTF8

if (-not $OutZip) {
    $OutZip = Join-Path $env:USERPROFILE "Downloads\MiddinInnovatie-Windows-$version.zip"
}
if (Test-Path $OutZip) { Remove-Item $OutZip -Force }

Write-Host "Creating ZIP (kan een minuut duren)..." -ForegroundColor Cyan
Push-Location $stage
try {
    tar -a -cf $OutZip *
    if ($LASTEXITCODE -ne 0) { throw "tar failed creating zip" }
} finally {
    Pop-Location
}
Remove-Item $stage -Recurse -Force -ErrorAction SilentlyContinue

$mb = [math]::Round((Get-Item $OutZip).Length / 1MB, 2)
Write-Host ""
Write-Host "ZIP ready ($mb MB): $OutZip" -ForegroundColor Green
Write-Host "Contains: setup, updater, uninstall, portable app (exe+jar+java)" -ForegroundColor Green
Start-Process explorer.exe -ArgumentList "/select,$OutZip"
