<#
.SYNOPSIS
    Copies a BRAVA source bundle to a server over ssh, unpacks it there and optionally deploys it.

.DESCRIPTION
    PowerShell version of deploy/copy-to-server.sh for Windows 10/11. Needs only built-in tools:
    tar.exe and the OpenSSH client (ssh.exe). Uses a single ssh connection, so a password is asked
    at most once (no prompt at all with an ssh key).

    On the server the bundle is unpacked into RemoteDir (default ~/brava); with -Deploy the image
    is built with podman (deploy/build-image.sh) and the systemd user service is installed and
    restarted (deploy/deploy.sh). The app then runs on http://<server>:10000/brapivalidator/

.PARAMETER Target
    ssh target, e.g. user@server.example.org

.PARAMETER Deploy
    Build the image and (re)start the service on the server after unpacking.

.PARAMETER Mode
    advanced (default) or simple; passed to deploy/deploy.sh.

.PARAMETER Bundle
    Existing bundle to copy (default: create a new one with make-bundle.ps1).

.PARAMETER RemoteDir
    Target directory on the server, relative to the home directory unless absolute (default: brava).

.PARAMETER SshArgs
    Extra ssh options, e.g. @('-p', '2222', '-i', "$HOME\.ssh\id_ed25519").

.EXAMPLE
    .\deploy\copy-to-server.ps1 user@server -Deploy

.EXAMPLE
    .\deploy\copy-to-server.ps1 user@server -Deploy -Mode simple -SshArgs @('-p', '2222')
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string]$Target,

    [switch]$Deploy,

    [ValidateSet('advanced', 'simple')]
    [string]$Mode = 'advanced',

    [string]$Bundle,

    [string]$RemoteDir = 'brava',

    [string[]]$SshArgs = @()
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$ssh = Get-Command ssh.exe -ErrorAction SilentlyContinue
if (-not $ssh) {
    throw 'ssh.exe not found. Install the OpenSSH client: Settings > System > Optional features > OpenSSH Client.'
}

if (-not $Bundle) {
    $Bundle = & (Join-Path $PSScriptRoot 'make-bundle.ps1') | Select-Object -Last 1
}
if (-not (Test-Path -PathType Leaf $Bundle)) { throw "Bundle not found: $Bundle" }
$Bundle = (Resolve-Path $Bundle).Path
$bundleName = Split-Path -Leaf $Bundle

if ($RemoteDir.StartsWith('~/')) { $RemoteDir = $RemoteDir.Substring(2) }
foreach ($v in @($RemoteDir, $bundleName)) {
    if ($v -match "['`"%]") { throw "Quotes and % are not allowed in: $v" }
}

$unpackArgs = "--mode $Mode"
if ($Deploy) { $unpackArgs = "--deploy $unpackArgs" }

# Same remote command as copy-to-server.sh: the bundle arrives on stdin, then the server unpacks
# it with deploy/unpack-bundle.sh taken from the bundle itself.
$remote = "set -e; mkdir -p '$RemoteDir'; cd '$RemoteDir'; cat > '$bundleName'; " +
          "tar -xzf '$bundleName' -O deploy/unpack-bundle.sh > .unpack-bundle.sh; " +
          "bash .unpack-bundle.sh '$bundleName' $unpackArgs"

function Quote([string]$s) {
    if ($s -match '[\s"]') { return '"' + ($s -replace '"', '\"') + '"' }
    return $s
}

# cmd.exe does the stdin redirection: Windows PowerShell 5.1 cannot pipe binary data to a native
# program. ssh reads the password from the console, not from stdin.
$parts = @((Quote $ssh.Source)) + @($SshArgs | ForEach-Object { Quote $_ }) + @((Quote $Target), "`"$remote`"")
$line = ($parts -join ' ') + " < `"$Bundle`""

Write-Host "Copying $bundleName to ${Target}:$RemoteDir/ and unpacking it there"
$psi = New-Object System.Diagnostics.ProcessStartInfo
$psi.FileName = Join-Path $env:SystemRoot 'System32\cmd.exe'
$psi.Arguments = '/d /s /c "' + $line + '"'
$psi.UseShellExecute = $false
$proc = [System.Diagnostics.Process]::Start($psi)
$proc.WaitForExit()
if ($proc.ExitCode -ne 0) { throw "Remote step failed (exit code $($proc.ExitCode))" }

if ($Deploy) {
    $hostName = ($Target -split '@')[-1]
    Write-Host ""
    Write-Host "Done. BRAVA ($Mode mode) should be available at http://${hostName}:10000/brapivalidator/"
}
