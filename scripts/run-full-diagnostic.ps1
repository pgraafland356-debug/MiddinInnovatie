# Full Middin Innovatie diagnostic - ASCII report to console.
# Usage: .\scripts\run-full-diagnostic.ps1
param([string]$MiddinRepo = "")

$ErrorActionPreference = "Continue"
if (-not $MiddinRepo) {
    $MiddinRepo = Split-Path -Parent $PSScriptRoot
}
$MiddinRepo = (Resolve-Path $MiddinRepo).Path

. (Join-Path $MiddinRepo "scripts\read-app-version.ps1")
. (Join-Path $MiddinRepo "scripts\read-github-update-config.ps1")

$issues = New-Object System.Collections.Generic.List[string]
$warnings = New-Object System.Collections.Generic.List[string]
$passed = New-Object System.Collections.Generic.List[string]

function Add-Pass([string]$m) { $script:passed.Add($m) }
function Add-Warn([string]$m) { $script:warnings.Add($m) }
function Add-Issue([string]$m) { $script:issues.Add($m) }

function Test-FileExists([string]$rel, [string]$label) {
    $p = Join-Path $MiddinRepo $rel
    if (Test-Path $p) { Add-Pass "$label" } else { Add-Issue "MISSING: $rel ($label)" }
}

Write-Host ""
Write-Host "+==================================================================+" -ForegroundColor White
Write-Host "|         MIDDIN INNOVATIE - FULL DIAGNOSTIC REPORT                |" -ForegroundColor White
Write-Host "+==================================================================+" -ForegroundColor White
Write-Host ("|  {0,-62}|" -f ("Date: " + (Get-Date -Format "yyyy-MM-dd HH:mm")))
Write-Host ("|  {0,-62}|" -f ("Repo: $MiddinRepo"))
Write-Host "+==================================================================+" -ForegroundColor White

# --- Version ---
Write-Host ""
Write-Host "[ VERSION ]" -ForegroundColor Cyan
try {
    $ver = Get-AppVersion -Root $MiddinRepo
    Add-Pass "app/build.gradle.kts -> $($ver.VersionName) / code $($ver.VersionCode)"
} catch {
    Add-Issue "Cannot read app version: $_"
    $ver = [pscustomobject]@{ VersionName = "?"; VersionCode = 0 }
}

$gh = Get-MiddinGithubUpdateConfig -Root $MiddinRepo
if ($gh.FeedUrl) { Add-Pass "Update feed configured: $($gh.Owner)/$($gh.Repo) @ $($gh.Branch)" }
else { Add-Issue "middin.github.owner not set in gradle.properties" }

# --- Required files ---
Write-Host ""
Write-Host "[ REQUIRED FILES ]" -ForegroundColor Cyan
$required = @(
    @("app\src\main\res\drawable-nodpi\middin_wordmark.png", "Android wordmark"),
    @("app\src\main\res\drawable-nodpi\middin_app_icon.jpg", "Android app icon"),
    @("desktop\src\main\resources\brand\middin_app_icon.jpg", "Desktop brand icon"),
    @("desktop\src\main\resources\brand\middin_wordmark.png", "Desktop wordmark"),
    @("desktop\installer\UpdateWizard.cs", "Update program source"),
    @("desktop\installer\InstallWizard.cs", "Install wizard source"),
    @("scripts\release-checklist.ps1", "Release checklist script"),
    @("scripts\create-windows-installer.ps1", "Windows installer build"),
    @("releases\latest.json", "Update manifest (local)")
)
foreach ($item in $required) { Test-FileExists $item[0] $item[1] }

$setupName = "MiddinInnovatie-Windows-Setup-$($ver.VersionName).exe"
Test-FileExists "desktop\build\installer\$setupName" "Windows setup build"
Test-FileExists "releases\artifacts\$setupName" "Release artifact setup"

# --- Manifest integrity ---
Write-Host ""
Write-Host "[ UPDATE MANIFEST ]" -ForegroundColor Cyan
$localManifest = Join-Path $MiddinRepo "releases\latest.json"
if (Test-Path $localManifest) {
    try {
        $json = Get-Content $localManifest -Raw
        if ($json -match '"versionCode"\s*:\s*(\d+)') {
            $mc = [int]$Matches[1]
            if ($mc -eq $ver.VersionCode) { Add-Pass "local latest.json versionCode matches gradle ($mc)" }
            else { Add-Warn "local latest.json code $mc != gradle $($ver.VersionCode)" }
        }
        $setupArtifact = Join-Path $MiddinRepo "releases\artifacts\$setupName"
        if (Test-Path $setupArtifact) {
            $winMatch = [regex]::Match($json, '"windows"\s*:\s*\{[^}]*"sha256"\s*:\s*"([a-f0-9]+)"')
            if ($winMatch.Success) {
                $expected = $winMatch.Groups[1].Value
                $actual = (Get-FileHash $setupArtifact -Algorithm SHA256).Hash.ToLower()
                if ($actual -eq $expected) { Add-Pass "Setup SHA-256 matches latest.json" }
                else { Add-Issue "Setup SHA-256 MISMATCH (artifact vs manifest)" }
            }
        }
    } catch { Add-Issue "Failed to parse local latest.json: $_" }
}

if ($gh.FeedUrl) {
    try {
        $remote = (Invoke-WebRequest -Uri ($gh.FeedUrl + "?t=" + [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()) -UseBasicParsing -TimeoutSec 20).Content
        if ($remote -match '"versionCode"\s*:\s*(\d+)') {
            $rc = [int]$Matches[1]
            if ($rc -eq $ver.VersionCode) { Add-Pass "GitHub feed versionCode = $rc (live)" }
            elseif ($rc -lt $ver.VersionCode) { Add-Warn "GitHub feed code $rc < local $($ver.VersionCode) (push main?)" }
            else { Add-Warn "GitHub feed code $rc > local $($ver.VersionCode)" }
        }
    } catch { Add-Warn "Cannot fetch GitHub feed: $_" }
}

# --- Installed app ---
Write-Host ""
Write-Host "[ LOCAL INSTALL ]" -ForegroundColor Cyan
$installDir = Join-Path $env:LOCALAPPDATA "Programs\MiddinInnovatie"
if (Test-Path $installDir) {
    Add-Pass "Install folder exists: $installDir"
    if (Test-Path (Join-Path $installDir "MiddinInnovatie.exe")) { Add-Pass "MiddinInnovatie.exe installed" }
    else { Add-Warn "MiddinInnovatie.exe not in install folder" }
    if (Test-Path (Join-Path $installDir "app\MiddinInnovatie.jar")) { Add-Pass "MiddinInnovatie.jar present" }
    else { Add-Issue "MISSING: installed MiddinInnovatie.jar" }
    $vf = Join-Path $installDir "version.json"
    if (Test-Path $vf) {
        $vj = Get-Content $vf -Raw
        if ($vj -match '"versionCode"\s*:\s*(\d+)') {
            $ic = [int]$Matches[1]
            if ($ic -lt $ver.VersionCode) { Add-Warn "Installed code $ic < project $($ver.VersionCode) - updater can offer update" }
            elseif ($ic -eq $ver.VersionCode) { Add-Pass "Installed version.json matches project code $ic" }
            else { Add-Warn "Installed code $ic > project $($ver.VersionCode)" }
        }
    } else { Add-Warn "No version.json in install folder (updater may not detect version)" }
    if (Test-Path (Join-Path $installDir "MiddinInnovatie-Update.exe")) { Add-Pass "Updater installed" }
    else { Add-Warn "MiddinInnovatie-Update.exe not in install folder" }
} else {
    Add-Warn "No local install at $installDir"
}

# --- Builds ---
Write-Host ""
Write-Host "[ BUILD CHECKS ]" -ForegroundColor Cyan
Push-Location $MiddinRepo
try {
    & .\gradlew.bat :desktop:compileJava --no-daemon 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) { Add-Pass "Gradle :desktop:compileJava" }
    else { Add-Issue "Gradle :desktop:compileJava FAILED" }

    & .\gradlew.bat :app:compileDebugKotlin --no-daemon 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) { Add-Pass "Gradle :app:compileDebugKotlin" }
    else { Add-Issue "Gradle :app:compileDebugKotlin FAILED (SSL/network?)" }
} finally { Pop-Location }

try {
    & (Join-Path $MiddinRepo "scripts\build-update-wizard-exe.ps1") 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) { Add-Pass "C# update wizard compiles" }
    else { Add-Issue "C# update wizard build FAILED" }
} catch { Add-Issue "C# update wizard: $_" }

# --- RSS / links ---
Write-Host ""
Write-Host "[ NETWORK - RSS & LINKS ]" -ForegroundColor Cyan
$linkScript = Join-Path $MiddinRepo "scripts\check-app-links.ps1"
if (Test-Path $linkScript) {
    $linkOut = & powershell -NoProfile -ExecutionPolicy Bypass -File $linkScript -RepoRoot $MiddinRepo 2>&1
    $linkText = $linkOut -join "`n"
    if ($linkText -match 'Result: (\d+) ok, (\d+) failed') {
        $ok = $Matches[1]; $fail = $Matches[2]
        if ([int]$fail -eq 0) { Add-Pass "RSS/fallback URLs: $ok ok, 0 failed" }
        else { Add-Warn "RSS/fallback URLs: $ok ok, $fail failed (app uses static fallback)" }
    }
}

# --- Summary ASCII ---
Write-Host ""
Write-Host "+==================================================================+" -ForegroundColor White
Write-Host "| SUMMARY                                                          |" -ForegroundColor White
Write-Host "+==================================================================+" -ForegroundColor White
Write-Host "| PASS ($($passed.Count))" -ForegroundColor Green
foreach ($p in $passed) { Write-Host ("|   [OK]   {0}" -f $p.Substring(0, [Math]::Min(58, $p.Length))) }
Write-Host "+------------------------------------------------------------------+" -ForegroundColor White
if ($warnings.Count -gt 0) {
    Write-Host "| WARN ($($warnings.Count))" -ForegroundColor Yellow
    foreach ($w in $warnings) { Write-Host ("|   [!!]   {0}" -f $w.Substring(0, [Math]::Min(58, $w.Length))) }
    Write-Host "+------------------------------------------------------------------+" -ForegroundColor White
}
if ($issues.Count -gt 0) {
    Write-Host "| FAIL ($($issues.Count))" -ForegroundColor Red
    foreach ($i in $issues) { Write-Host ("|   [XX]   {0}" -f $i.Substring(0, [Math]::Min(58, $i.Length))) }
    Write-Host "+------------------------------------------------------------------+" -ForegroundColor White
}
$status = if ($issues.Count -eq 0) { "HEALTHY (with $($warnings.Count) warning(s))" } else { "NEEDS ATTENTION ($($issues.Count) issue(s))" }
Write-Host ("| STATUS: {0}" -f $status) -ForegroundColor $(if ($issues.Count -eq 0) { "Green" } else { "Red" })
Write-Host "+==================================================================+" -ForegroundColor White
Write-Host ""

if ($issues.Count -gt 0) { exit 1 }
exit 0
