[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"
$runtimeDir = Join-Path $PSScriptRoot ".demo-runtime"
$pidFile = Join-Path $runtimeDir "processes.json"

function Stop-ProcessTree {
    param([int]$ProcessId)

    $children = Get-CimInstance Win32_Process -Filter "ParentProcessId = $ProcessId" -ErrorAction SilentlyContinue
    foreach ($child in $children) {
        Stop-ProcessTree -ProcessId $child.ProcessId
    }

    Stop-Process -Id $ProcessId -Force -ErrorAction SilentlyContinue
}

if (-not (Test-Path -LiteralPath $pidFile)) {
    Write-Host "No demo process record was found."
    exit 0
}

$records = @(Get-Content -Raw -LiteralPath $pidFile | ConvertFrom-Json)
foreach ($record in $records) {
    $process = Get-Process -Id $record.Id -ErrorAction SilentlyContinue
    if ($process) {
        Write-Host "Stopping $($record.Name) (PID $($record.Id))..."
        Stop-ProcessTree -ProcessId $record.Id
    }
}

Remove-Item -LiteralPath $pidFile -Force -ErrorAction SilentlyContinue
Write-Host "Demo services started by Start-Demo have been stopped."
