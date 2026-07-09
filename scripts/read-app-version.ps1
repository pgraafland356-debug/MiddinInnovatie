# Read / write app version from app/build.gradle.kts (single source of truth).

function Get-AppVersion {
    param([string]$Root)
    if (-not $Root) {
        $Root = Split-Path -Parent $PSScriptRoot
    }
    $appGradlePath = Join-Path $Root "app\build.gradle.kts"
    if (-not (Test-Path $appGradlePath)) {
        throw "Not found: $appGradlePath"
    }
    $appGradle = Get-Content $appGradlePath -Raw
    $nameMatch = [regex]::Match($appGradle, 'versionName\s*=\s*"([^"]+)"')
    $codeMatch = [regex]::Match($appGradle, 'versionCode\s*=\s*(\d+)')
    if (-not $nameMatch.Success) { throw "versionName not found in app/build.gradle.kts" }
    if (-not $codeMatch.Success) { throw "versionCode not found in app/build.gradle.kts" }
    [pscustomobject]@{
        VersionName = $nameMatch.Groups[1].Value
        VersionCode = [int]$codeMatch.Groups[1].Value
    }
}

function Set-AppVersion {
    param(
        [string]$Root,
        [string]$VersionName,
        [int]$VersionCode
    )
    if (-not $Root) {
        $Root = Split-Path -Parent $PSScriptRoot
    }
    $appGradlePath = Join-Path $Root "app\build.gradle.kts"
    $desktopGradlePath = Join-Path $Root "desktop\build.gradle.kts"
    $appGradle = Get-Content $appGradlePath -Raw
    $appGradle = [regex]::Replace($appGradle, 'versionCode\s*=\s*\d+', "versionCode = $VersionCode")
    $appGradle = [regex]::Replace($appGradle, 'versionName\s*=\s*"[^"]*"', "versionName = `"$VersionName`"")
    [System.IO.File]::WriteAllText($appGradlePath, $appGradle.TrimEnd() + "`n")

    if (Test-Path $desktopGradlePath) {
        $desktopGradle = Get-Content $desktopGradlePath -Raw
        $desktopGradle = [regex]::Replace($desktopGradle, 'version\s*=\s*"[^"]*"', "version = `"$VersionName`"")
        [System.IO.File]::WriteAllText($desktopGradlePath, $desktopGradle.TrimEnd() + "`n")
    }
}

function Get-NextPatchVersion {
    param([string]$VersionName)
    $parts = $VersionName.Split('.')
    if ($parts.Length -lt 2) { throw "Invalid versionName: $VersionName" }
    $last = [int]$parts[$parts.Length - 1]
    $parts[$parts.Length - 1] = ($last + 1).ToString()
    return ($parts -join '.')
}

if ($MyInvocation.InvocationName -ne '.') {
    Get-AppVersion
}
