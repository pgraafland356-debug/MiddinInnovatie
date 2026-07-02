# Minimal portable JRE (java.desktop) for Middin Innovatie — no separate Java install needed.
param(
    [string]$OutDir = ""
)

$ErrorActionPreference = "Stop"
$repo = Split-Path -Parent $PSScriptRoot
if (-not $OutDir) {
    $OutDir = Join-Path $repo "desktop\build\jre-portable"
}

if (-not $env:JAVA_HOME) {
    throw "JAVA_HOME is not set. Point it to JDK 17+ before building the installer."
}

$jlink = Join-Path $env:JAVA_HOME "bin\jlink.exe"
if (-not (Test-Path $jlink)) {
    throw "jlink not found at $jlink"
}

if (Test-Path $OutDir) {
    Remove-Item $OutDir -Recurse -Force
}

Write-Host "Building portable JRE: $OutDir" -ForegroundColor Cyan
& $jlink `
    --add-modules java.desktop `
    --strip-debug `
    --no-man-pages `
    --no-header-files `
    --output $OutDir
if ($LASTEXITCODE -ne 0) { throw "jlink failed" }

$javaw = Join-Path $OutDir "bin\javaw.exe"
if (-not (Test-Path $javaw)) { throw "Portable JRE invalid: missing javaw.exe" }

$mb = [math]::Round((Get-ChildItem $OutDir -Recurse -File | Measure-Object Length -Sum).Sum / 1MB, 1)
Write-Host "Portable JRE ready ($mb MB): $OutDir" -ForegroundColor Green
