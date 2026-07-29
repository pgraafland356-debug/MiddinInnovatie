# Copy releases/changelog.json into app assets and desktop resources.
param(
    [string]$RepoRoot = ""
)

$ErrorActionPreference = "Stop"
if (-not $RepoRoot) {
    $RepoRoot = Split-Path -Parent $PSScriptRoot
}

$src = Join-Path $RepoRoot "releases\changelog.json"
if (-not (Test-Path $src)) {
    throw "Missing $src. Run update-changelog.ps1 or create releases/changelog.json first."
}

$targets = @(
    (Join-Path $RepoRoot "app\src\main\assets\changelog.json"),
    (Join-Path $RepoRoot "app\src\test\resources\changelog.json"),
    (Join-Path $RepoRoot "desktop\src\main\resources\changelog.json")
)

foreach ($dest in $targets) {
    $dir = Split-Path -Parent $dest
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
    }
    Copy-Item $src $dest -Force
    Write-Host "Synced changelog -> $dest" -ForegroundColor Green
}
