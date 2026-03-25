# Start mock API on port 8080 (all interfaces). Ctrl+C to stop.
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot
python .\mock_server.py
