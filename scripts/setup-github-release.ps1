# One-time GitHub setup + publish release v0.9.2 for auto-updates.
# Prerequisite: gh auth login  (run this script; it will prompt if needed)

$ErrorActionPreference = "Stop"
$repo = Split-Path -Parent $PSScriptRoot
$owner = "pgraafland356-debug"
$repoName = "MiddinInnovatie"
$version = "0.9.2"
$tag = "v$version"
Push-Location $repo

function Find-Gh {
    $paths = @(
        "$env:ProgramFiles\GitHub CLI\gh.exe",
        "$env:LocalAppData\Programs\GitHub CLI\gh.exe"
    )
    foreach ($p in $paths) {
        if (Test-Path $p) { return $p }
    }
    $cmd = Get-Command gh -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    throw "GitHub CLI (gh) not found."
}

try {
    $gh = Find-Gh
    Write-Host "GitHub CLI: $gh" -ForegroundColor Cyan

    $auth = & $gh auth status 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Log in to GitHub (browser will open)..." -ForegroundColor Yellow
        & $gh auth login --hostname github.com --web --git-protocol https
        if ($LASTEXITCODE -ne 0) { throw "GitHub login failed. Run: gh auth login" }
    }

    $artifactDir = Join-Path $repo "releases\artifacts"
    $apk = Join-Path $artifactDir "middin-innovatie-$version.apk"
    $setup = Join-Path $artifactDir "MiddinInnovatie-Windows-Setup-$version.exe"
    if (-not (Test-Path $setup)) {
        Write-Host "Building Windows installer..." -ForegroundColor Cyan
        & (Join-Path $repo "scripts\create-windows-installer.ps1")
        New-Item -ItemType Directory -Path $artifactDir -Force | Out-Null
        Copy-Item (Join-Path $repo "desktop\build\installer\MiddinInnovatie-Windows-Setup-$version.exe") $setup -Force
    }
    if (-not (Test-Path $apk)) {
        Write-Host "Building Android APK (debug, for beta distribution)..." -ForegroundColor Cyan
        & .\gradlew.bat :app:assembleDebug --no-daemon
        Copy-Item "app\build\outputs\apk\debug\app-debug.apk" $apk -Force
    }

    $apkHash = (Get-FileHash $apk -Algorithm SHA256).Hash.ToLower()
    $setupHash = (Get-FileHash $setup -Algorithm SHA256).Hash.ToLower()
    $manifest = @{
        versionName = $version
        versionCode = 11
        changelog = "Middin Innovatie $version - productcatalogus (18 producten), MEMO Timer, Windows installer met Java, auto-update."
        android = @{
            apkUrl = "https://github.com/$owner/$repoName/releases/download/$tag/middin-innovatie-$version.apk"
            sha256 = $apkHash
        }
        windows = @{
            setupUrl = "https://github.com/$owner/$repoName/releases/download/$tag/MiddinInnovatie-Windows-Setup-$version.exe"
            sha256 = $setupHash
        }
    } | ConvertTo-Json -Depth 4
    Set-Content (Join-Path $repo "releases\latest.json") $manifest -Encoding UTF8
    $utf8NoBom = New-Object System.Text.UTF8Encoding $false
    [System.IO.File]::WriteAllText((Join-Path $repo "releases\latest.json"), $manifest, $utf8NoBom)
    Write-Host "Updated releases/latest.json" -ForegroundColor Green

    $hasOrigin = git remote 2>$null | Select-String -Pattern '^origin$' -Quiet
    if (-not $hasOrigin) {
        Write-Host "Creating GitHub repo $owner/$repoName ..." -ForegroundColor Cyan
        & $gh repo create "$owner/$repoName" --public --description "Middin Innovatie app (Android + Windows)" --source . --remote origin
        if ($LASTEXITCODE -ne 0) {
            & $gh repo create "$repoName" --public --description "Middin Innovatie app" --source . --remote origin
        }
    }

    Write-Host "Pushing code (main)..." -ForegroundColor Cyan
    git add releases/latest.json gradle.properties scripts/ app/ desktop/ releases/ gradle/ settings.gradle.kts build.gradle.kts .gitignore
    git add -u
    git commit -m "Add GitHub auto-update feed and release manifest for v0.9.2." 2>$null
    if ($LASTEXITCODE -ne 0) { Write-Host "Nothing new to commit or commit skipped." -ForegroundColor Yellow }
    git branch -M main 2>$null
    git push -u origin main
    if ($LASTEXITCODE -ne 0) { git push -u origin master }

    Write-Host "Creating GitHub Release $tag ..." -ForegroundColor Cyan
    & $gh release create $tag `
        --title "Middin Innovatie $version" `
        --notes "Eerste release met auto-update. Windows: volledige setup EXE. Android: APK. Feed: releases/latest.json op main." `
        $apk $setup
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Release may already exist; uploading assets..." -ForegroundColor Yellow
        & $gh release upload $tag $apk $setup --clobber
    }

    git add releases/latest.json
    git commit -m "Update release manifest SHA256 for $tag" 2>$null
    git push

    Write-Host ""
    Write-Host "Done! Update feed:" -ForegroundColor Green
    Write-Host "  https://raw.githubusercontent.com/$owner/$repoName/main/releases/latest.json"
    Write-Host "  https://github.com/$owner/$repoName/releases/tag/$tag"
} finally {
    Pop-Location
}
