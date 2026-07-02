# Build MiddinInnovatie.exe (C# launcher that starts javaw -jar app\MiddinInnovatie.jar)
param(
    [string]$OutDir = ""
)

$ErrorActionPreference = "Stop"
$repo = Split-Path -Parent $PSScriptRoot
$src = Join-Path $repo "desktop\launcher\Program.cs"
if (-not (Test-Path $src)) { throw "Missing $src" }

if (-not $OutDir) {
    $OutDir = Join-Path $repo "desktop\build\dist\MiddinInnovatie"
}

$csc = Join-Path $env:WINDIR "Microsoft.NET\Framework64\v4.0.30319\csc.exe"
if (-not (Test-Path $csc)) {
    $csc = Join-Path $env:WINDIR "Microsoft.NET\Framework\v4.0.30319\csc.exe"
}
if (-not (Test-Path $csc)) {
    throw "csc.exe not found (.NET Framework). Use Start Middin Innovatie.bat instead."
}

New-Item -ItemType Directory -Path $OutDir -Force | Out-Null
$outExe = Join-Path $OutDir "MiddinInnovatie.exe"
$tempExe = Join-Path $env:TEMP ("MiddinInnovatie-launcher-" + [guid]::NewGuid().ToString("N") + ".exe")
Write-Host "Building launcher: $outExe" -ForegroundColor Cyan
& $csc /nologo /target:winexe /reference:System.Windows.Forms.dll /out:$tempExe $src
if ($LASTEXITCODE -ne 0) { throw "csc failed" }

# Stop running app instances so the output exe is not locked (jpackage wrapper may linger).
Get-Process -Name "MiddinInnovatie","javaw","java" -ErrorAction SilentlyContinue |
    Where-Object { $_.Path -like "*MiddinInnovatie*" -or $_.MainWindowTitle -like "*Middin*" } |
    Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Milliseconds 500

if (Test-Path $outExe) {
    $backup = Join-Path $OutDir "MiddinInnovatie-jpackage-old.exe"
    if (-not (Test-Path $backup)) {
        try { Rename-Item $outExe $backup -Force -ErrorAction Stop } catch { Remove-Item $outExe -Force -ErrorAction SilentlyContinue }
    } else {
        Remove-Item $outExe -Force -ErrorAction SilentlyContinue
    }
}
Move-Item $tempExe $outExe -Force
Write-Host "Launcher built: $outExe" -ForegroundColor Green
