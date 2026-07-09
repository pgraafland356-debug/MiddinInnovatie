<#
.SYNOPSIS
  HTTP-check every https:// URL embedded in RSS sources and static fallback news items.

.DESCRIPTION
  Parses InnovationRssSources.kt and InnovationNewsRepository.kt so the list stays in sync with code.
  Exit code 0 if all checks pass, 1 if any URL fails or files are missing.

  Also prints a separate WARNING-ONLY section for API_BASE_URL / UPDATE_FEED_URL in app/build.gradle.kts
  (placeholders, emulator loopback, optional soft reachability). That section never affects the exit code.

  Linux/macOS: pwsh scripts/check-app-links.ps1

.PARAMETER RepoRoot
  Repository root (folder containing app/). Defaults to parent of scripts/.
#>
param(
    [string]$RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
)

$ErrorActionPreference = 'Stop'

$files = @(
    (Join-Path $RepoRoot 'app\src\main\java\com\middin\innovatie\app\data\rss\InnovationRssSources.kt'),
    (Join-Path $RepoRoot 'app\src\main\java\com\middin\innovatie\app\data\InnovationNewsRepository.kt')
)

$pattern = 'https://[^"\s]+'
$urls = [System.Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)

foreach ($path in $files) {
    if (-not (Test-Path -LiteralPath $path)) {
        Write-Error "Missing source file: $path"
        exit 1
    }
    $text = Get-Content -LiteralPath $path -Raw
    foreach ($m in [regex]::Matches($text, $pattern)) {
        $u = $m.Value.TrimEnd(')', ',', ';', '"')
        if ($u -notmatch '^https://') { continue }
        # Skip XML namespace noise if ever pasted into these files
        if ($u -match 'schemas\.android\.com') { continue }
        [void]$urls.Add($u)
    }
}

$sorted = $urls | Sort-Object
Write-Host "Checking $($sorted.Count) unique URL(s) from RSS + fallback sources..." -ForegroundColor Cyan

$failures = New-Object System.Collections.Generic.List[object]
$ok = 0

foreach ($url in $sorted) {
    try {
        $resp = Invoke-WebRequest -Uri $url -Method Get -MaximumRedirection 8 -TimeoutSec 30 `
            -UseBasicParsing -ErrorAction Stop `
            -Headers @{ 'User-Agent' = 'MiddinInnovatie/1.0 (link-check)' }
        $code = [int]$resp.StatusCode
        if ($code -ge 200 -and $code -lt 400) {
            Write-Host "  OK $code  $url" -ForegroundColor Green
            $ok++
        }
        else {
            Write-Host "  BAD $code  $url" -ForegroundColor Yellow
            $failures.Add([pscustomobject]@{ Url = $url; Status = $code; Error = 'Unexpected status' })
        }
    }
    catch {
        $code = $null
        if ($_.Exception.Response) {
            try { $code = [int]$_.Exception.Response.StatusCode.value__ } catch {}
        }
        $msg = $_.Exception.Message -replace '[\r\n]+', ' '
        Write-Host "  FAIL $(if ($code) { $code } else { '--' })  $url" -ForegroundColor Red
        Write-Host "       $msg" -ForegroundColor DarkRed
        $failures.Add([pscustomobject]@{ Url = $url; Status = $code; Error = $msg })
    }
}

Write-Host ""
Write-Host "Result: $ok ok, $($failures.Count) failed (of $($sorted.Count) URLs)." -ForegroundColor $(if ($failures.Count -eq 0) { 'Green' } else { 'Red' })

if ($failures.Count -gt 0) {
    exit 1
}

# --- Build config URLs: warnings only (never fails this script) ---
Write-Host ""
Write-Host "--- Build config URLs (warnings only; exit code unaffected) ---" -ForegroundColor DarkCyan

$gradlePath = Join-Path $RepoRoot 'app\build.gradle.kts'
if (-not (Test-Path -LiteralPath $gradlePath)) {
    Write-Host "  [WARN] Missing $gradlePath - skipping build config URL section." -ForegroundColor Yellow
    exit 0
}

$gradleText = Get-Content -LiteralPath $gradlePath -Raw
# Match field name + third arg value (handles multiline buildConfigField blocks).
$gradleRx = [regex]::new('"(API_BASE_URL|UPDATE_FEED_URL)"\s*,\s*"\\"([^"]*)\\""')
foreach ($m in $gradleRx.Matches($gradleText)) {
    $field = $m.Groups[1].Value
    $value = $m.Groups[2].Value.Trim()
    if ([string]::IsNullOrWhiteSpace($value)) {
        Write-Host "  [WARN] $field uses an empty string in a buildConfigField - intentional?" -ForegroundColor Yellow
        continue
    }
    if ($value -match '^https?://10\.0\.2\.2') {
        Write-Host "  [WARN] $field = $value" -ForegroundColor Yellow
        Write-Host "         Emulator-only loopback (host PC). Not verified from this script." -ForegroundColor DarkYellow
        continue
    }
    if ($value -match 'example\.(com|org|net)\b') {
        Write-Host "  [WARN] $field = $value" -ForegroundColor Yellow
        Write-Host "         Placeholder / documentation host - replace for production." -ForegroundColor DarkYellow
        try {
            $null = Invoke-WebRequest -Uri $value -Method Get -MaximumRedirection 3 -TimeoutSec 15 `
                -UseBasicParsing -ErrorAction Stop
            Write-Host "         Reachability: responded (unusual for a placeholder)." -ForegroundColor DarkYellow
        }
        catch {
            Write-Host "         Reachability: not reachable - typical for placeholders." -ForegroundColor DarkGray
        }
        continue
    }
    Write-Host "  [INFO] $field = $value (optional check)" -ForegroundColor DarkCyan
    try {
        $resp = Invoke-WebRequest -Uri $value -Method Get -MaximumRedirection 8 -TimeoutSec 25 `
            -UseBasicParsing -ErrorAction Stop
        Write-Host "         OK $($resp.StatusCode)" -ForegroundColor DarkGreen
    }
    catch {
        $msg = $_.Exception.Message -replace '[\r\n]+', ' '
        Write-Host "  [WARN] $field not reachable: $msg" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Build config section complete (warnings only)." -ForegroundColor DarkCyan
