# Creates Middin Innovatie Windows installer (single EXE + optional zip).

$ErrorActionPreference = "Stop"
$version = "0.9.2"
$repo = Split-Path -Parent $PSScriptRoot
Push-Location $repo

try {
    Write-Host "Building desktop JAR..." -ForegroundColor Cyan
    & .\gradlew.bat :desktop:fatJar --no-daemon
    if ($LASTEXITCODE -ne 0) { throw "Gradle fatJar failed" }

    $jar = Join-Path $repo "desktop\build\libs\desktop-$version-all.jar"
    if (-not (Test-Path $jar)) {
        $jar = Get-ChildItem (Join-Path $repo "desktop\build\libs") -Filter "*-all.jar" |
            Sort-Object LastWriteTime -Descending |
            Select-Object -First 1 -ExpandProperty FullName
    }
    if (-not $jar) { throw "Fat jar not found" }

    & (Join-Path $repo "scripts\build-portable-jre.ps1")
    $jreDir = Join-Path $repo "desktop\build\jre-portable"

    $portableDir = Join-Path $repo "desktop\build\dist\MiddinInnovatie"
    $appJarDir = Join-Path $portableDir "app"
    $runtimeDir = Join-Path $portableDir "runtime"
    New-Item -ItemType Directory -Path $appJarDir -Force | Out-Null
    Copy-Item $jar (Join-Path $appJarDir "MiddinInnovatie.jar") -Force
    if (Test-Path $runtimeDir) { Remove-Item $runtimeDir -Recurse -Force }
    Copy-Item $jreDir $runtimeDir -Recurse -Force

    & (Join-Path $repo "scripts\build-launcher-exe.ps1") -OutDir $portableDir
    Copy-Item (Join-Path $repo "scripts\installer\Start-MiddinInnovatie.bat") (Join-Path $portableDir "Start Middin Innovatie.bat") -Force

    $stage = Join-Path $repo "desktop\build\installer-stage"
    $payload = Join-Path $stage "payload"
    $outDir = Join-Path $repo "desktop\build\installer"
    if (Test-Path $stage) { Remove-Item $stage -Recurse -Force }
    New-Item -ItemType Directory -Path (Join-Path $payload "app") -Force | Out-Null

    Copy-Item (Join-Path $portableDir "app\MiddinInnovatie.jar") (Join-Path $payload "app\MiddinInnovatie.jar") -Force
    Copy-Item (Join-Path $portableDir "MiddinInnovatie.exe") (Join-Path $payload "MiddinInnovatie.exe") -Force
    Copy-Item (Join-Path $portableDir "Start Middin Innovatie.bat") (Join-Path $payload "Start Middin Innovatie.bat") -Force
    Write-Host "Copying portable Java runtime into installer payload..." -ForegroundColor Cyan
    Copy-Item (Join-Path $portableDir "runtime") (Join-Path $payload "runtime") -Recurse -Force

    Get-Process -Name java,javaw -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2

    $payloadZip = Join-Path $env:TEMP ("MiddinInnovatie-payload-" + [guid]::NewGuid().ToString("N") + ".zip")
    Write-Host "Packing installer payload (this can take a minute)..." -ForegroundColor Cyan
    Push-Location $payload
    try {
        tar -a -cf $payloadZip .
        if ($LASTEXITCODE -ne 0) { throw "tar failed packing payload" }
    } finally {
        Pop-Location
    }

    $standaloneName = "MiddinInnovatie-Windows-Setup-$version.exe"
    & (Join-Path $repo "scripts\build-install-wizard-exe.ps1") `
        -OutDir $outDir `
        -OutExeName $standaloneName `
        -PayloadZip $payloadZip
    Remove-Item $payloadZip -Force -ErrorAction SilentlyContinue

    $standaloneExe = Join-Path $outDir $standaloneName
    try {
        Copy-Item $standaloneExe (Join-Path $repo $standaloneName) -Force
    } catch {
        $alt = Join-Path $repo ("MiddinInnovatie-Windows-Setup-$version-new.exe")
        Copy-Item $standaloneExe $alt -Force
        Write-Host "Note: project root copy locked; saved as $alt" -ForegroundColor Yellow
    }
    $downloads = Join-Path $env:USERPROFILE "Downloads"
    $downloadsExe = Join-Path $downloads $standaloneName
    try {
        Copy-Item $standaloneExe $downloadsExe -Force
    } catch {
        $alt = Join-Path $downloads ("MiddinInnovatie-Windows-Setup-$version-new.exe")
        Copy-Item $standaloneExe $alt -Force
        $downloadsExe = $alt
        Write-Host "Note: Downloads copy locked; saved as $alt" -ForegroundColor Yellow
    }

    # Folder installer (dev / zip distribution)
    & (Join-Path $repo "scripts\build-install-wizard-exe.ps1") -OutDir $stage -OutExeName "MiddinInnovatie-Setup.exe"
    Copy-Item (Join-Path $repo "scripts\installer\install.ps1") (Join-Path $stage "install.ps1") -Force
    Copy-Item (Join-Path $repo "scripts\installer\INSTALL.bat") (Join-Path $stage "INSTALL.bat") -Force
    Copy-Item (Join-Path $repo "scripts\installer\Start-MiddinInnovatie.bat") (Join-Path $stage "Start-MiddinInnovatie.bat") -Force
    @"
Middin Innovatie v$version
========================
Aanbevolen: dubbelklik MiddinInnovatie-Windows-Setup-$version.exe

Alternatief (na uitpakken zip):
- INSTALL.bat
- of MiddinInnovatie-Setup.exe (wizard naast map payload)
"@ | Set-Content (Join-Path $stage "LEESMIJ.txt") -Encoding UTF8

    $zipPath = Join-Path $outDir "MiddinInnovatie-Windows-Installer-$version.zip"
    Get-Process -Name java,javaw -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
    Start-Sleep -Milliseconds 500
    if (Test-Path $zipPath) { Remove-Item $zipPath -Force }
    Write-Host "Creating zip installer..." -ForegroundColor Cyan
    Compress-Archive -Path "$stage\*" -DestinationPath $zipPath -Force
    Copy-Item $zipPath (Join-Path $repo "MiddinInnovatie-Windows-Installer-$version.zip") -Force

    $exeMb = [math]::Round((Get-Item $standaloneExe).Length / 1MB, 2)
    $downloadsExe = Join-Path $downloads $standaloneName
    Write-Host ""
    Write-Host "Installer EXE ready ($exeMb MB) - includes Java, no separate install needed:" -ForegroundColor Green
    Write-Host "  $standaloneExe"
    Write-Host "  $(Join-Path $repo $standaloneName)"
    Write-Host "  $downloadsExe"
    Start-Process explorer.exe -ArgumentList "/select,$downloadsExe"
} finally {
    Pop-Location
}
