# Build Middin Innovatie install wizard EXE (optional embedded payload = single-file installer)
param(
    [string]$OutDir = "",
    [string]$OutExeName = "MiddinInnovatie-Setup.exe",
    [string]$PayloadZip = ""
)

$ErrorActionPreference = "Stop"
$repo = Split-Path -Parent $PSScriptRoot
$src = Join-Path $repo "desktop\installer\InstallWizard.cs"
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
$tempExe = Join-Path $env:TEMP ("MiddinInnovatie-Setup-" + [guid]::NewGuid().ToString("N") + ".exe")

$args = @(
    "/nologo", "/target:winexe",
    "/reference:System.Windows.Forms.dll",
    "/reference:System.Drawing.dll",
    "/reference:System.IO.Compression.FileSystem.dll",
    "/out:$tempExe"
)
if ($PayloadZip -and (Test-Path $PayloadZip)) {
    $args += "/resource:$PayloadZip,MiddinInnovatie.Payload.zip"
    Write-Host "Embedding payload: $PayloadZip" -ForegroundColor Cyan
}
$args += $src

Write-Host "Building install wizard: $outExe" -ForegroundColor Cyan
& $csc @args
if ($LASTEXITCODE -ne 0) { throw "csc failed" }
if (Test-Path $outExe) { Remove-Item $outExe -Force -ErrorAction SilentlyContinue }
Move-Item $tempExe $outExe -Force
Write-Host "Setup wizard built: $outExe" -ForegroundColor Green
