# Copies dev-accounts/dev-accounts.json to Android assets and desktop resources.
$ErrorActionPreference = "Stop"
$repo = Split-Path -Parent $PSScriptRoot
$src = Join-Path $repo "dev-accounts\dev-accounts.json"
if (-not (Test-Path $src)) { throw "Missing $src" }
$targets = @(
    (Join-Path $repo "app\src\main\assets\dev-accounts.json"),
    (Join-Path $repo "desktop\src\main\resources\dev-accounts.json")
)
foreach ($t in $targets) {
    $dir = Split-Path $t -Parent
    New-Item -ItemType Directory -Path $dir -Force | Out-Null
    Copy-Item $src $t -Force
    Write-Host "Synced: $t" -ForegroundColor Green
}
