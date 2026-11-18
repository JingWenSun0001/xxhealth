[CmdletBinding()]
param(
    [switch]$NoBrowser,
    [switch]$RebuildBackend
)

$ErrorActionPreference = "Stop"
$projectRoot = $PSScriptRoot
$runtimeDir = Join-Path $projectRoot ".demo-runtime"
$pidFile = Join-Path $runtimeDir "processes.json"

New-Item -ItemType Directory -Force -Path $runtimeDir | Out-Null

function Test-LocalPort {
    param([int]$Port)

    $client = New-Object System.Net.Sockets.TcpClient
    try {
        $result = $client.BeginConnect("127.0.0.1", $Port, $null, $null)
        if (-not $result.AsyncWaitHandle.WaitOne(300)) {
            return $false
        }
        $client.EndConnect($result)
        return $true
    }
    catch {
        return $false
    }
    finally {
        $client.Dispose()
    }
}

function Wait-LocalPort {
    param(
        [int]$Port,
        [int]$TimeoutSeconds = 90
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-LocalPort -Port $Port) {
            return $true
        }
        Start-Sleep -Milliseconds 500
    }
    return $false
}

function Start-LoggedProcess {
    param(
        [string]$Name,
        [string]$FilePath,
        [string[]]$ArgumentList,
        [string]$WorkingDirectory
    )

    $stdout = Join-Path $runtimeDir "$Name.out.log"
    $stderr = Join-Path $runtimeDir "$Name.err.log"
    $process = Start-Process `
        -FilePath $FilePath `
        -ArgumentList $ArgumentList `
        -WorkingDirectory $WorkingDirectory `
        -RedirectStandardOutput $stdout `
        -RedirectStandardError $stderr `
        -WindowStyle Hidden `
        -PassThru

    return [pscustomobject]@{
        Name = $Name
        Id = $process.Id
        StartedAt = (Get-Date).ToString("o")
    }
}

function Find-Browser {
    $candidates = @(
        (Join-Path ${env:ProgramFiles(x86)} "Microsoft\Edge\Application\msedge.exe"),
        (Join-Path $env:ProgramFiles "Microsoft\Edge\Application\msedge.exe"),
        (Join-Path $env:LOCALAPPDATA "Microsoft\Edge\Application\msedge.exe"),
        (Join-Path $env:ProgramFiles "Google\Chrome\Application\chrome.exe"),
        (Join-Path ${env:ProgramFiles(x86)} "Google\Chrome\Application\chrome.exe"),
        (Join-Path $env:LOCALAPPDATA "Google\Chrome\Application\chrome.exe")
    )

    return $candidates | Where-Object { $_ -and (Test-Path -LiteralPath $_) } | Select-Object -First 1
}

$npm = Get-Command npm.cmd -ErrorAction SilentlyContinue
if (-not $npm) {
    throw "npm.cmd was not found. Install Node.js and try again."
}

$launched = @()

if (-not (Test-LocalPort -Port 8088)) {
    if ($RebuildBackend) {
        Write-Host "Building backend..."
        & (Join-Path $projectRoot "mvnw.cmd") -q -DskipTests package
        if ($LASTEXITCODE -ne 0) {
            throw "Backend build failed."
        }
    }

    $jar = $null
    if (Test-Path -LiteralPath (Join-Path $projectRoot "target")) {
        $jar = Get-ChildItem -LiteralPath (Join-Path $projectRoot "target") -Filter "*.jar" |
            Where-Object { $_.Name -notlike "*.original" } |
            Sort-Object LastWriteTime -Descending |
            Select-Object -First 1
    }

    if (-not $jar) {
        Write-Host "Backend package not found; building it now..."
        & (Join-Path $projectRoot "mvnw.cmd") -q -DskipTests package
        if ($LASTEXITCODE -ne 0) {
            throw "Backend build failed."
        }
        $jar = Get-ChildItem -LiteralPath (Join-Path $projectRoot "target") -Filter "*.jar" |
            Where-Object { $_.Name -notlike "*.original" } |
            Select-Object -First 1
    }

    $java = Get-Command java.exe -ErrorAction SilentlyContinue
    if (-not $java) {
        throw "java.exe was not found. Install Java and try again."
    }

    Write-Host "Starting backend on port 8088..."
    $launched += Start-LoggedProcess -Name "backend" -FilePath $java.Source -ArgumentList @("-jar", $jar.FullName) -WorkingDirectory $projectRoot
}
else {
    Write-Host "Backend is already running on port 8088."
}

if (-not (Test-LocalPort -Port 8080)) {
    Write-Host "Starting user client on port 8080..."
    $launched += Start-LoggedProcess -Name "user-client" -FilePath $npm.Source -ArgumentList @("run", "serve", "--", "--host", "127.0.0.1") -WorkingDirectory (Join-Path $projectRoot "mobile-client\xxhealth-fronted")
}
else {
    Write-Host "User client is already running on port 8080."
}

if (-not (Test-LocalPort -Port 8081)) {
    Write-Host "Starting doctor client on port 8081..."
    $launched += Start-LoggedProcess -Name "doctor-client" -FilePath $npm.Source -ArgumentList @("run", "serve", "--", "--host", "127.0.0.1") -WorkingDirectory (Join-Path $projectRoot "xxhealth-fronted")
}
else {
    Write-Host "Doctor client is already running on port 8081."
}

if ($launched.Count -gt 0) {
    $knownProcesses = @()
    if (Test-Path -LiteralPath $pidFile) {
        $knownProcesses = @(Get-Content -Raw -LiteralPath $pidFile | ConvertFrom-Json) |
            Where-Object { Get-Process -Id $_.Id -ErrorAction SilentlyContinue }
    }
    @($knownProcesses) + @($launched) |
        ConvertTo-Json |
        Set-Content -LiteralPath $pidFile -Encoding UTF8
}

$services = @(
    @{ Name = "backend"; Port = 8088 },
    @{ Name = "user client"; Port = 8080 },
    @{ Name = "doctor client"; Port = 8081 }
)

$failed = @()
foreach ($service in $services) {
    Write-Host "Waiting for $($service.Name)..."
    if (-not (Wait-LocalPort -Port $service.Port)) {
        $failed += $service.Name
    }
}

if ($failed.Count -gt 0) {
    throw "These services did not become ready: $($failed -join ', '). Logs: $runtimeDir"
}

$userUrl = "http://127.0.0.1:8080/#/login"
$doctorUrl = "http://127.0.0.1:8081/#/doctor/dashboard"

if (-not $NoBrowser) {
    $browser = Find-Browser
    if ($browser) {
        Start-Process -FilePath $browser -ArgumentList @("--app=$userUrl", "--new-window", "--window-size=430,900", "--window-position=40,50")
        Start-Sleep -Milliseconds 500
        Start-Process -FilePath $browser -ArgumentList @("--app=$doctorUrl", "--new-window", "--window-size=1350,900", "--window-position=500,50")
    }
    else {
        Start-Process $userUrl
        Start-Process $doctorUrl
    }
}

Write-Host ""
Write-Host "Demo is ready."
Write-Host "User client:   $userUrl"
Write-Host "Doctor client: $doctorUrl"
Write-Host "Stop services: double-click Stop-Demo.cmd"
