# Build release artifacts and generate releases/latest.json for GitHub.
# Usage:
#   1. Set middin.github.owner in gradle.properties
#   2. Bump versionCode in app/build.gradle.kts and AppVersion.java
#   3. .\scripts\publish-github-release.ps1
#   4. Create GitHub Release tag v{version}, upload APK + Setup EXE, commit latest.json

param(
    [string]$VersionName = "0.9.2",
    [int]$VersionCode = 11,
    [string]$Changelog = "Middin Innovatie release $VersionName"
)

$ErrorActionPreference = "Stop"
$repo = Split-Path -Parent $PSScriptRoot
Push-Location $repo

function Get-Sha256Hex([string]$Path) {
    return (Get-FileHash -Path $Path -Algorithm SHA256).Hash.ToLowerInvariant()
}

try {
    $props = Get-Content (Join-Path $repo "gradle.properties") -Raw
    if ($props -match 'middin\.github\.owner=(.+)') {
        $owner = $Matches[1].Trim()
    } else {
        $owner = "YOUR_GITHUB_USERNAME"
    }
    if ($props -match 'middin\.github\.repo=(.+)') {
        $repoName = $Matches[1].Trim()
    } else {
        $repoName = "MiddinInnovatie"
    }
    if ($owner -eq "YOUR_GITHUB_USERNAME" -or [string]::IsNullOrWhiteSpace($owner)) {
        Write-Host "Set middin.github.owner in gradle.properties first." -ForegroundColor Yellow
    }

    Write-Host "Building Android release APK..." -ForegroundColor Cyan
    & .\gradlew.bat :app:assembleRelease --no-daemon
    if ($LASTEXITCODE -ne 0) { throw "assembleRelease failed" }

    $apkSrc = Join-Path $repo "app\build\outputs\apk\release\app-release.apk"
    if (-not (Test-Path $apkSrc)) { throw "APK not found: $apkSrc" }

    $outDir = Join-Path $repo "releases\artifacts"
    New-Item -ItemType Directory -Path $outDir -Force | Out-Null
    $apkName = "middin-innovatie-$VersionName.apk"
    $apkOut = Join-Path $outDir $apkName
    Copy-Item $apkSrc $apkOut -Force

    Write-Host "Building Windows setup..." -ForegroundColor Cyan
    & (Join-Path $repo "scripts\create-windows-installer.ps1")
    if ($LASTEXITCODE -ne 0) { throw "create-windows-installer failed" }

    $setupName = "MiddinInnovatie-Windows-Setup-$VersionName.exe"
    $setupSrc = Join-Path $repo "desktop\build\installer\$setupName"
    if (-not (Test-Path $setupSrc)) { throw "Setup EXE not found: $setupSrc" }
    $setupOut = Join-Path $outDir $setupName
    Copy-Item $setupSrc $setupOut -Force

    $tag = "v$VersionName"
    $baseUrl = "https://github.com/$owner/$repoName/releases/download/$tag"
    $manifest = @{
        versionName = $VersionName
        versionCode = $VersionCode
        changelog = $Changelog
        android = @{
            apkUrl = "$baseUrl/$apkName"
            sha256 = (Get-Sha256Hex $apkOut)
        }
        windows = @{
            setupUrl = "$baseUrl/$setupName"
            sha256 = (Get-Sha256Hex $setupOut)
        }
    } | ConvertTo-Json -Depth 4

    $manifestPath = Join-Path $repo "releases\latest.json"
    $utf8NoBom = New-Object System.Text.UTF8Encoding $false
    [System.IO.File]::WriteAllText($manifestPath, $manifest, $utf8NoBom)

    Write-Host ""
    Write-Host "Artifacts:" -ForegroundColor Green
    Write-Host "  $apkOut"
    Write-Host "  $setupOut"
    Write-Host "  $manifestPath"
    Write-Host ""
    Write-Host "Next steps on GitHub:" -ForegroundColor Cyan
    Write-Host "  1. Commit and push releases/latest.json"
    Write-Host "  2. New Release tag: $tag"
    Write-Host "  3. Upload: $apkName and $setupName"
    Write-Host "  4. Colleagues get updates via Meer -> Instellingen -> Controleren op update"
} finally {
    Pop-Location
}
