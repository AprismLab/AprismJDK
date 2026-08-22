# Requires elevation. One-shot WSL2 enabler for AprismJDK fork builds.
# Run AFTER a convenient reboot point: enables VirtualMachinePlatform,
# installs WSL from Store, then registers Ubuntu LTS.
#
# Usage: right-click -> Run with Administrator, or:
#   Start-Process powershell -Verb RunAs -ArgumentList '-File scripts\setup-wsl2.ps1'

$ErrorActionPreference = 'Stop'
if (-not ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()
        ).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    throw 'Run elevated.'
}

Write-Host '=== AprismJDK WSL2 setup ===' -ForegroundColor Cyan

# 1. Enable required Windows features (no reboot forced; flag set for later)
dism.exe /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart
dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart

# 2. WSL kernel + default version 2
wsl.exe --install --no-distribution
wsl.exe --set-default-version 2

# 3. Register Ubuntu LTS (downloads ~650 MB)
wsl.exe --install -d Ubuntu --no-launch

Write-Host ''
Write-Host 'Components installed. REBOOT REQUIRED.' -ForegroundColor Yellow
Write-Host 'After reboot:'
Write-Host '  1. wsl -d Ubuntu            (finish first-run user setup)'
Write-Host '  2. wsl -d Ubuntu -e bash scripts/wsl2-builddeps.sh'
Write-Host '  3. wsl -d Ubuntu -e bash scripts/build-openjdk-wsl.sh'
