# Build Middin Innovatie uninstall wizard EXE
param(
    [string]$OutDir = "",
    [string]$OutExeName = "MiddinInnovatie-Uninstall.exe"
)

$ErrorActionPreference = "Stop"
$repo = Split-Path -Parent $PSScriptRoot
& (Join-Path $repo "scripts\gen-installer-urls.ps1")
$srcDir = Join-Path $repo "desktop\installer"
$src = Join-Path $srcDir "UninstallWizard.cs"
$urls = Join-Path $srcDir "InstallerUrls.cs"
if (-not (Test-Path $src)) { throw "Missing $src" }

if (-not $OutDir) {
    $OutDir = Join-Path $repo "desktop\build\installer-stage"
}

$csc = Join-Path $env:WINDIR "Microsoft.NET\Framework64\v4.0.30319\csc.exe"
if (-not (Test-Path $csc)) {
    $csc = Join-Path $env:WINDIR "Microsoft.NET\Framework\v4.0.30319\csc.exe"
}
if (-not (Test-Path $csc)) { throw "csc.exe not found (.NET Framework)." }

New-Item -ItemType Directory -Path $OutDir -Force | Out-Null
$outExe = Join-Path $OutDir $OutExeName
$tempExe = Join-Path $env:TEMP ("MiddinInnovatie-Uninstall-" + [guid]::NewGuid().ToString("N") + ".exe")

Write-Host "Building uninstall wizard: $outExe" -ForegroundColor Cyan
& $csc /nologo /target:winexe /reference:System.Windows.Forms.dll /reference:System.Drawing.dll /out:$tempExe $urls $src
if ($LASTEXITCODE -ne 0) { throw "csc failed" }
if (Test-Path $outExe) { Remove-Item $outExe -Force -ErrorAction SilentlyContinue }
Move-Item $tempExe $outExe -Force
Write-Host "Uninstall wizard built: $outExe" -ForegroundColor Green
