# Build Middin Innovatie Windows desktop app + installer (jpackage)

param(
    [switch]$JarOnly,
    [ValidateSet("app-image", "exe", "msi", "all")]
    [string]$InstallerType = "app-image"
)

$ErrorActionPreference = "Stop"
$repo = Split-Path -Parent $PSScriptRoot
$version = "0.9.2"
Push-Location $repo

function Find-Jpackage {
    $jp = Get-Command jpackage -ErrorAction SilentlyContinue
    if ($jp) { return $jp.Source }
    if ($env:JAVA_HOME) {
        $candidate = Join-Path $env:JAVA_HOME "bin\jpackage.exe"
        if (Test-Path $candidate) { return $candidate }
    }
    return $null
}

function Invoke-JpackageBuild {
    param(
        [string]$Jpackage,
        [string]$Stage,
        [string]$Dest,
        [string]$Type
    )
    Write-Host "jpackage --type $Type ..." -ForegroundColor Cyan
    $args = @(
        "--type", $Type,
        "--input", $Stage,
        "--dest", $Dest,
        "--name", "MiddinInnovatie",
        "--main-jar", "MiddinInnovatie.jar",
        "--main-class", "com.middin.innovatie.desktop.Main",
        "--add-modules", "java.desktop",
        "--app-version", $version,
        "--vendor", "Middin",
        "--description", "Middin Innovatie - productcatalogus en productassistent voor Windows",
        "--copyright", "Middin"
    )
    if ($Type -eq "exe" -or $Type -eq "msi") {
        $args += @(
            "--win-menu",
            "--win-menu-group", "Middin Innovatie",
            "--win-shortcut",
            "--win-shortcut-prompt"
        )
    }
    & $Jpackage @args
    if ($LASTEXITCODE -ne 0) {
        throw "jpackage --type $Type failed (exit $LASTEXITCODE)"
    }
}

try {
    Write-Host "Building desktop jar..." -ForegroundColor Cyan
    .\gradlew.bat :desktop:fatJar --no-daemon
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

    $jar = Join-Path $repo "desktop\build\libs\desktop-$version-all.jar"
    if (-not (Test-Path $jar)) {
        $jar = Get-ChildItem (Join-Path $repo "desktop\build\libs") -Filter "*-all.jar" |
            Sort-Object LastWriteTime -Descending |
            Select-Object -First 1 -ExpandProperty FullName
    }
    if (-not $jar) { throw "Fat jar not found in desktop\build\libs" }
    Write-Host "Jar: $jar" -ForegroundColor Green

    if ($JarOnly) {
        Write-Host "Done (-JarOnly). Run: java -jar `"$jar`"" -ForegroundColor Yellow
        exit 0
    }

    $jpackage = Find-Jpackage
    if (-not $jpackage) {
        Write-Host "jpackage not found (needs JDK 17+). Use the jar instead:" -ForegroundColor Yellow
        Write-Host "  java -jar `"$jar`""
        exit 0
    }

    $stage = Join-Path $repo "desktop\build\jpackage-stage"
    $out = Join-Path $repo "desktop\build\dist"
    $appImageDir = Join-Path $out "MiddinInnovatie"
    if (Test-Path $stage) { Remove-Item $stage -Recurse -Force }
    if (Test-Path $appImageDir) { Remove-Item $appImageDir -Recurse -Force }
    New-Item -ItemType Directory -Path $stage -Force | Out-Null
    Copy-Item $jar (Join-Path $stage "MiddinInnovatie.jar")

    $types = @()
    switch ($InstallerType) {
        "all" { $types = @("app-image") }
        default { $types = @($InstallerType) }
    }

    foreach ($type in $types) {
        try {
            Invoke-JpackageBuild -Jpackage $jpackage -Stage $stage -Dest $out -Type $type
        } catch {
            if ($type -eq "exe" -or $type -eq "msi") {
                Write-Host "Note: $type installer needs WiX Toolset on PATH. Use scripts\create-windows-installer.ps1 instead." -ForegroundColor Yellow
            } else {
                throw
            }
        }
    }

    $appImageDir = Join-Path $out "MiddinInnovatie"
    if (Test-Path $appImageDir) {
        Write-Host "Building reliable MiddinInnovatie.exe launcher..." -ForegroundColor Cyan
        & (Join-Path $repo "scripts\build-launcher-exe.ps1") -OutDir $appImageDir
        Copy-Item (Join-Path $repo "scripts\installer\Start-MiddinInnovatie.bat") (Join-Path $appImageDir "Start Middin Innovatie.bat") -Force -ErrorAction SilentlyContinue
    }

    Write-Host ""
    Write-Host "Build complete (Windows 64-bit, JDK 17 runtime included)." -ForegroundColor Green

    $appExe = Join-Path $out "MiddinInnovatie\MiddinInnovatie.exe"
    if (Test-Path $appExe) {
        Write-Host "  Portable app:  $appExe"
    }

    $setupExe = Get-ChildItem $out -Filter "MiddinInnovatie-$version.exe" -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if ($setupExe) {
        Write-Host "  Installer:     $($setupExe.FullName)"
        $rootCopy = Join-Path $repo "MiddinInnovatie-Windows-setup-$version.exe"
        Copy-Item $setupExe.FullName $rootCopy -Force
        Write-Host "  Copy:          $rootCopy"
    }

    $msi = Get-ChildItem $out -Filter "MiddinInnovatie-$version.msi" -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if ($msi) {
        Write-Host "  MSI installer: $($msi.FullName)"
    }
} finally {
    Pop-Location
}
