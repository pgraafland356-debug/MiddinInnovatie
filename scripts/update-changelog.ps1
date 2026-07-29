# Append or update a release entry in releases/changelog.json (newest first).
param(
    [Parameter(Mandatory = $true)][string]$VersionName,
    [Parameter(Mandatory = $true)][int]$VersionCode,
    [Parameter(Mandatory = $true)][string]$Changelog,
    [string]$DateIso = "",
    [string]$RepoRoot = ""
)

$ErrorActionPreference = "Stop"
if (-not $RepoRoot) {
    $RepoRoot = Split-Path -Parent $PSScriptRoot
}
if (-not $DateIso) {
    $DateIso = (Get-Date).ToString("yyyy-MM-dd")
}

function Split-ChangelogBullets([string]$Text) {
    $lines = @()
    foreach ($line in ($Text -split "[\r\n]+")) {
        $trimmed = $line.Trim()
        if ($trimmed) { $lines += $trimmed }
    }
    if ($lines.Count -eq 1) {
        $single = $lines[0]
        if ($single -match '^\s*Middin Innovatie\s+[\d.]+\s*-\s*(.+)$') {
            $rest = $Matches[1].Trim()
            if ($rest -match ',') {
                return @($rest -split ',\s*' | ForEach-Object { $_.Trim() } | Where-Object { $_ })
            }
            return @($rest)
        }
    }
    return $lines
}

$path = Join-Path $RepoRoot "releases\changelog.json"
$entries = @()
if (Test-Path $path) {
    $doc = Get-Content $path -Raw -Encoding UTF8 | ConvertFrom-Json
    if ($doc.entries) {
        $entries = @($doc.entries)
    }
}

$entries = @($entries | Where-Object { [int]$_.versionCode -ne $VersionCode })
$newEntry = [ordered]@{
    versionCode = $VersionCode
    versionName = $VersionName
    dateIso     = $DateIso
    bullets     = @(Split-ChangelogBullets $Changelog)
}
$entries = @($newEntry) + @($entries | Sort-Object { -[int]$_.versionCode })

$out = @{ entries = $entries } | ConvertTo-Json -Depth 6
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllText($path, $out, $utf8NoBom)
Write-Host "Updated $path ($VersionName / $VersionCode)" -ForegroundColor Green
