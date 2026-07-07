# Shared GitHub update URLs from gradle.properties (single source of truth).
param(
    [string]$RepoRoot = ""
)

function Get-MiddinGithubUpdateConfig {
    param([string]$Root)
    if (-not $Root) {
        $Root = Split-Path -Parent $PSScriptRoot
    }
    $propsPath = Join-Path $Root "gradle.properties"
    $owner = ""
    $repo = "MiddinInnovatie"
    if (Test-Path $propsPath) {
        $props = Get-Content $propsPath -Raw
        if ($props -match 'middin\.github\.owner=(.+)') {
            $owner = $Matches[1].Trim()
        }
        if ($props -match 'middin\.github\.repo=(.+)') {
            $repo = $Matches[1].Trim()
        }
    }
    if ([string]::IsNullOrWhiteSpace($repo)) { $repo = "MiddinInnovatie" }
    $feedUrl = ""
    $releasesUrl = ""
    if ($owner -and -not $owner.StartsWith("YOUR_")) {
        $feedUrl = "https://raw.githubusercontent.com/$owner/$repo/main/releases/latest.json"
        $releasesUrl = "https://github.com/$owner/$repo/releases"
    }
    [pscustomobject]@{
        Owner       = $owner
        Repo        = $repo
        FeedUrl     = $feedUrl
        ReleasesUrl = $releasesUrl
    }
}

if ($MyInvocation.InvocationName -ne '.') {
    Get-MiddinGithubUpdateConfig -Root $RepoRoot
}
