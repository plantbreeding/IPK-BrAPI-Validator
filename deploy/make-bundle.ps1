<#
.SYNOPSIS
    Creates a BRAVA source bundle (tar.gz) for building the container image on the server.

.DESCRIPTION
    PowerShell version of deploy/make-bundle.sh for Windows 10/11 (uses the built-in tar.exe).
    The bundle contains python/, frontend/, the test collections and schemas and deploy/, without
    .venv, .env, local databases, node_modules or frontend/dist. Returns the path of the bundle.

.PARAMETER OutputDir
    Directory for the bundle (default: <repo>\build).

.EXAMPLE
    .\deploy\make-bundle.ps1
#>
[CmdletBinding()]
param(
    [string]$OutputDir
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
if (-not $OutputDir) { $OutputDir = Join-Path $root 'build' }

# Built-in Windows tar (bsdtar); a GNU tar from Git Bash on PATH would take C:\ paths as hosts.
$tar = Join-Path $env:SystemRoot 'System32\tar.exe'
if (-not (Test-Path $tar)) { throw "tar.exe not found at $tar (needs Windows 10 1803 or later)" }

Push-Location $root
try {
    $rev = 'nogit'
    if (Get-Command git -ErrorAction SilentlyContinue) {
        $out = & git rev-parse --short HEAD 2>$null
        if ($LASTEXITCODE -eq 0 -and $out) {
            $rev = "$out".Trim()
            $dirty = & git status --porcelain 2>$null
            if ($dirty) { $rev = "$rev-dirty" }
        }
    }
    $stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
    $name = "brava-bundle-$stamp-$rev"

    New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null
    $bundle = Join-Path (Resolve-Path $OutputDir).Path "$name.tar.gz"

    $tmp = Join-Path ([System.IO.Path]::GetTempPath()) ([System.IO.Path]::GetRandomFileName())
    New-Item -ItemType Directory -Path $tmp | Out-Null
    try {
        $created = (Get-Date).ToUniversalTime().ToString('yyyy-MM-ddTHH:mm:ssZ')
        # LF line endings and no BOM: the file is read on Linux.
        $info = "bundle=$name`ncommit=$rev`ncreated=$created`n"
        [System.IO.File]::WriteAllText((Join-Path $tmp 'BUNDLE_INFO'), $info, (New-Object System.Text.UTF8Encoding $false))

        $tarArgs = @(
            '-czf', $bundle,
            '--exclude', 'python/.venv',
            '--exclude', 'python/.env',
            '--exclude', 'python/*.db',
            '--exclude', 'python/.pytest_cache',
            '--exclude', 'python/.ruff_cache',
            '--exclude', '__pycache__',
            '--exclude', '*.pyc',
            '--exclude', 'frontend/node_modules',
            '--exclude', 'frontend/dist',
            '--exclude', 'frontend/.env*',
            'python',
            'frontend',
            'src/main/resources/collections',
            'src/main/resources/schemas',
            'deploy',
            '-C', $tmp, 'BUNDLE_INFO'
        )
        & $tar @tarArgs
        if ($LASTEXITCODE -ne 0) { throw "tar failed with exit code $LASTEXITCODE" }
    }
    finally {
        Remove-Item -Recurse -Force $tmp -ErrorAction SilentlyContinue
    }
}
finally {
    Pop-Location
}

$size = '{0:N0} KB' -f ((Get-Item $bundle).Length / 1KB)
Write-Host "Created bundle ($size):"
Write-Host $bundle
$bundle
