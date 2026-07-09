# Middin Innovatie release checklist - bump version, build, manifest, optional GitHub publish.
#
# Examples:
#   .\scripts\release-checklist.ps1                    # build current version (0.9.4 / 13)
#   .\scripts\release-checklist.ps1 -BumpPatch         # 0.9.4 -> 0.9.5, code +1, then build
#   .\scripts\release-checklist.ps1 -VersionName "1.0.0" -VersionCode 20 -Changelog "..."
#   .\scripts\release-checklist.ps1 -BumpPatch -Publish   # also create GitHub release + push manifest
#   .\scripts\release-checklist.ps1 -DeployLocal       # copy new build into local install folder
#
param(
    [string]$VersionName = "",
    [int]$VersionCode = 0,
    [switch]$BumpPatch,
    [string]$Changelog = "",
    [switch]$Publish,
    [switch]$DeployLocal,
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"
$repo = Split-Path -Parent $PSScriptRoot
. (Join-Path $repo "scripts\read-app-version.ps1")
. (Join-Path $repo "scripts\read-github-update-config.ps1")

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
    return $null
}

function Write-Step([string]$Text, [string]$Status = "") {
    if ($Status -eq "OK") {
        Write-Host "[x] $Text" -ForegroundColor Green
    } elseif ($Status -eq "SKIP") {
        Write-Host "[ ] $Text (overgeslagen)" -ForegroundColor DarkGray
    } elseif ($Status -eq "TODO") {
        Write-Host "[ ] $Text" -ForegroundColor Yellow
    } else {
        Write-Host ">> $Text" -ForegroundColor Cyan
    }
}

Push-Location $repo
try {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor White
    Write-Host " Middin Innovatie - Release checklist" -ForegroundColor White
    Write-Host "========================================" -ForegroundColor White
    Write-Host ""

    $current = Get-AppVersion -Root $repo
    $targetName = $VersionName
    $targetCode = $VersionCode

    if ($BumpPatch) {
        if ($targetName) { throw 'Use -BumpPatch OR -VersionName - not both.' }
        $targetName = Get-NextPatchVersion -VersionName $current.VersionName
        $targetCode = $current.VersionCode + 1
    }

    if (-not $targetName) { $targetName = $current.VersionName }
    if ($targetCode -le 0) { $targetCode = $current.VersionCode }

    if ($targetName -ne $current.VersionName -or $targetCode -ne $current.VersionCode) {
        Write-Step "Versie bijwerken: $($current.VersionName) ($($current.VersionCode)) -> $targetName ($targetCode)"
        Set-AppVersion -Root $repo -VersionName $targetName -VersionCode $targetCode
        Write-Step "Versie in app/build.gradle.kts + desktop/build.gradle.kts" "OK"
    } else {
        Write-Step "Versie: $targetName (code $targetCode)" "OK"
    }

    if (-not $Changelog) {
        $Changelog = "Middin Innovatie $targetName"
    }

    $gh = Get-MiddinGithubUpdateConfig -Root $repo
    if ([string]::IsNullOrWhiteSpace($gh.Owner) -or $gh.Owner.StartsWith("YOUR_")) {
        Write-Step "gradle.properties: middin.github.owner instellen" "TODO"
    } else {
        Write-Step "GitHub feed: $($gh.FeedUrl)" "OK"
    }

    if (-not $SkipBuild) {
        Write-Step "Bouwen: APK + Windows setup + latest.json"
        & (Join-Path $repo "scripts\publish-github-release.ps1") `
            -VersionName $targetName `
            -VersionCode $targetCode `
            -Changelog $Changelog
        if ($LASTEXITCODE -ne 0) { throw "publish-github-release failed" }
        Write-Step "Build + releases/latest.json" "OK"
    } else {
        Write-Step "Build" "SKIP"
    }

    $artifactDir = Join-Path $repo "releases\artifacts"
    $setupName = "MiddinInnovatie-Windows-Setup-$targetName.exe"
    $apkName = "middin-innovatie-$targetName.apk"
    $setupArtifact = Join-Path $artifactDir $setupName
    $updateSrc = Join-Path $repo "desktop\build\installer-stage\MiddinInnovatie-Update.exe"
    $downloads = Join-Path $env:USERPROFILE "Downloads"

    if (Test-Path $setupArtifact) {
        try {
            Copy-Item $setupArtifact (Join-Path $downloads $setupName) -Force
            Write-Step "Setup gekopieerd naar Downloads\$setupName" "OK"
        } catch {
            Write-Step "Downloads kopie mislukt (bestand open?): $($_.Exception.Message)" "TODO"
        }
    }
    if (Test-Path $updateSrc) {
        try {
            Copy-Item $updateSrc (Join-Path $downloads "MiddinInnovatie-Update.exe") -Force
            Write-Step "Updater gekopieerd naar Downloads\MiddinInnovatie-Update.exe" "OK"
        } catch {
            Write-Step "Updater naar Downloads mislukt" "TODO"
        }
    }

    if ($DeployLocal) {
        $installDir = Join-Path $env:LOCALAPPDATA "Programs\MiddinInnovatie"
        if (Test-Path $installDir) {
            Get-Process -Name java, javaw -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
            Start-Sleep -Seconds 1
            $jarSrc = Join-Path $repo "desktop\build\libs\desktop-$targetName-all.jar"
            if (-not (Test-Path $jarSrc)) {
                $jarSrc = Get-ChildItem (Join-Path $repo "desktop\build\libs") -Filter "*-all.jar" |
                    Sort-Object LastWriteTime -Descending | Select-Object -First 1 -ExpandProperty FullName
            }
            if ($jarSrc) {
                Copy-Item $jarSrc (Join-Path $installDir "app\MiddinInnovatie.jar") -Force
                $versionJson = "{ `"versionName`": `"$targetName`", `"versionCode`": $targetCode }"
                [System.IO.File]::WriteAllText((Join-Path $installDir "version.json"), $versionJson + "`n")
                Write-Step "Lokale installatie bijgewerkt: $installDir" "OK"
            }
            if (Test-Path $updateSrc) {
                Copy-Item $updateSrc (Join-Path $installDir "MiddinInnovatie-Update.exe") -Force
            }
        } else {
            Write-Step "Geen lokale installatie gevonden ($installDir)" "SKIP"
        }
    }

    $tag = "v$targetName"
    $published = $false

    if ($Publish) {
        $ghExe = Find-Gh
        if (-not $ghExe) { throw "GitHub CLI (gh) not found. Install gh or run without -Publish." }
        & $ghExe auth status 2>&1 | Out-Null
        if ($LASTEXITCODE -ne 0) {
            throw "Run: gh auth login"
        }

        Write-Step "GitHub release aanmaken: $tag"
        $apk = Join-Path $artifactDir $apkName
        $setup = Join-Path $artifactDir $setupName
        if (-not (Test-Path $apk)) { throw "Missing $apk" }
        if (-not (Test-Path $setup)) { throw "Missing $setup" }

        & $ghExe release create $tag `
            --title "Middin Innovatie $targetName" `
            --notes $Changelog `
            $apk $setup
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Release bestaat mogelijk al - assets uploaden..." -ForegroundColor Yellow
            & $ghExe release upload $tag $apk $setup --clobber
            if ($LASTEXITCODE -ne 0) { throw "gh release upload failed" }
        }

        git add releases/latest.json app/build.gradle.kts desktop/build.gradle.kts
        git diff --cached --quiet
        if ($LASTEXITCODE -ne 0) {
            git commit -m "Release $tag - update manifest and version"
            git push
        }
        Write-Step "GitHub release $tag gepubliceerd" "OK"
        $published = $true
    }

    Write-Host ""
    Write-Host "========================================" -ForegroundColor White
    Write-Host " Klaar - $targetName (code $targetCode)" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor White
    Write-Host ""
    Write-Host "Artifacts:" -ForegroundColor Cyan
    Write-Host "  releases\artifacts\$apkName"
    Write-Host "  releases\artifacts\$setupName"
    Write-Host "  releases\latest.json"
    Write-Host ""
    Write-Host "Colleagues:" -ForegroundColor Cyan
    Write-Host "  - MiddinInnovatie-Update.exe  (of Start-menu > bijwerken)"
    Write-Host "  - App > Meer > Instellingen > Controleren op update"
    Write-Host ""

    if (-not $published) {
        Write-Host "Nog handmatig (zonder -Publish):" -ForegroundColor Yellow
        Write-Host "  1. git add releases/latest.json app/build.gradle.kts desktop/build.gradle.kts"
        Write-Host "  2. git commit -m ""Release $tag"""
        Write-Host "  3. git push"
        Write-Host "  4. GitHub > Releases > New > tag $tag"
        Write-Host "  5. Upload: $apkName en $setupName"
        Write-Host ""
        Write-Host "Of alles in één keer:" -ForegroundColor Yellow
        Write-Host "  .\scripts\release-checklist.ps1 -VersionName $targetName -VersionCode $targetCode -Publish"
        Write-Host ""
    }
} finally {
    Pop-Location
}
