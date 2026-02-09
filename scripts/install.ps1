#Requires -Version 5.1
<#
.SYNOPSIS
    Installs Maestro on Windows.
.DESCRIPTION
    Downloads the latest Maestro Windows build from GitHub releases,
    extracts it, and adds it to the user's PATH.
.PARAMETER Version
    Specific version to install (e.g. "2.1.0"). Defaults to latest.
#>
param(
    [string]$Version
)

$ErrorActionPreference = "Stop"

$installDir = Join-Path $env:LOCALAPPDATA "Maestro"

if ($Version) {
    $downloadUrl = "https://github.com/mobile-dev-inc/maestro/releases/download/cli-$Version/maestro-win.zip"
} else {
    $downloadUrl = "https://github.com/mobile-dev-inc/maestro/releases/latest/download/maestro-win.zip"
}

$tempDir = Join-Path $env:TEMP "maestro-install"
$zipFile = Join-Path $tempDir "maestro-win.zip"

# Create temp directory
Write-Host "* Creating temporary directory..."
New-Item -ItemType Directory -Force -Path $tempDir | Out-Null

# Download
Write-Host "* Downloading Maestro..."
try {
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    Invoke-WebRequest -Uri $downloadUrl -OutFile $zipFile -UseBasicParsing
} catch {
    Write-Error "Failed to download Maestro from $downloadUrl. Check your internet connection and try again."
    exit 1
}

# Remove previous installation
if (Test-Path $installDir) {
    Write-Host "* Removing previous installation..."
    Remove-Item -Recurse -Force $installDir
}

# Extract
Write-Host "* Extracting archive..."
Expand-Archive -Path $zipFile -DestinationPath $installDir -Force

# Clean up temp files
Write-Host "* Cleaning up..."
Remove-Item -Recurse -Force $tempDir

# Add to PATH if not already present
$userPath = [Environment]::GetEnvironmentVariable("Path", "User")
if ($userPath -notlike "*$installDir*") {
    Write-Host "* Adding Maestro to your PATH..."
    [Environment]::SetEnvironmentVariable("Path", "$userPath;$installDir", "User")
    $env:Path = "$env:Path;$installDir"
}

Write-Host ""
Write-Host "Installation was successful!" -ForegroundColor Green
Write-Host ""
Write-Host "Please restart your terminal, then run:"
Write-Host ""
Write-Host "    maestro --version"
Write-Host ""
Write-Host "Welcome to Maestro!"
