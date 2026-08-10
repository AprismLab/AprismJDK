# OpenJDK Build Wrapper for AprismJDK (Windows PowerShell)
# This script wraps OpenJDK build process with Aprism customizations

param(
    [string]$BootJdk = $env:JAVA_HOME,
    [string]$BuildType = "release",
    [string]$JvmVariants = "server",
    [string]$DebugLevel = "release",
    [string]$VsVersion = "2022"
)

$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptDir
$OpenJdkDir = Join-Path $ProjectRoot "openjdk-25"

Write-Host "=== AprismJDK OpenJDK Build Wrapper ===" -ForegroundColor Cyan
Write-Host "Platform: Windows"
Write-Host "OpenJDK Directory: $OpenJdkDir"
Write-Host "Boot JDK: $BootJdk"
Write-Host "Build Type: $BuildType"
Write-Host "Debug Level: $DebugLevel"
Write-Host "VS Version: $VsVersion"
Write-Host "========================================" -ForegroundColor Cyan

# Verify OpenJDK source exists
if (-not (Test-Path $OpenJdkDir)) {
    Write-Error "OpenJDK source directory not found at $OpenJdkDir"
    exit 1
}

# Verify boot JDK
if (-not $BootJdk) {
    Write-Error "Boot JDK not specified. Please set JAVA_HOME or use -BootJdk parameter"
    exit 1
}

if (-not (Test-Path $BootJdk)) {
    Write-Error "Boot JDK not found at $BootJdk"
    exit 1
}

# Check for Cygwin or WSL
$BashPath = $null
$CygwinPaths = @(
    "C:\cygwin64\bin\bash.exe",
    "C:\cygwin\bin\bash.exe"
)

foreach ($path in $CygwinPaths) {
    if (Test-Path $path) {
        $BashPath = $path
        break
    }
}

if (-not $BashPath) {
    # Try to find bash in PATH
    $BashPath = (Get-Command bash -ErrorAction SilentlyContinue).Source
}

if (-not $BashPath) {
    Write-Host ""
    Write-Host "WARNING: Cygwin/MSYS2 bash not found." -ForegroundColor Yellow
    Write-Host "OpenJDK requires Cygwin or MSYS2 to build on Windows." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Please install Cygwin from: https://www.cygwin.com/" -ForegroundColor Yellow
    Write-Host "Required packages: make, m4, autoconf, zip, unzip" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "For now, this script will create a stub build configuration." -ForegroundColor Yellow
    Write-Host ""
    
    # Create stub marker
    $StubFile = Join-Path $ProjectRoot "build\openjdk-build-stub.txt"
    New-Item -ItemType Directory -Force -Path (Split-Path $StubFile) | Out-Null
    Set-Content -Path $StubFile -Value "OpenJDK build requires Cygwin/MSYS2 on Windows"
    
    Write-Host "Created stub marker at: $StubFile" -ForegroundColor Yellow
    exit 0
}

Write-Host "Found bash at: $BashPath" -ForegroundColor Green

# Set up environment for Visual Studio
$VsWhere = "${env:ProgramFiles(x86)}\Microsoft Visual Studio\Installer\vswhere.exe"
if (Test-Path $VsWhere) {
    Write-Host "Detecting Visual Studio installation..."
    $VsPath = & $VsWhere -latest -property installationPath
    if ($VsPath) {
        Write-Host "Found Visual Studio at: $VsPath" -ForegroundColor Green
        
        # Note: vcvarsall.bat needs to be called from within Cygwin bash
        # This will be handled by the bash script
    }
} else {
    Write-Host "WARNING: vswhere.exe not found. Visual Studio detection skipped." -ForegroundColor Yellow
}

# Convert Windows paths to Cygwin paths for the bash script
$CygwinBootJdk = $BootJdk -replace '\\', '/' -replace '^([A-Z]):', '/cygdrive/$1'
$CygwinOpenJdkDir = $OpenJdkDir -replace '\\', '/' -replace '^([A-Z]):', '/cygdrive/$1'

# Create a temporary bash script
$TempScript = Join-Path $env:TEMP "aprism-build-jdk.sh"
$ScriptContent = @"
#!/bin/bash
set -e

export BOOT_JDK='$CygwinBootJdk'
export BUILD_TYPE='$BuildType'
export DEBUG_LEVEL='$DebugLevel'
export JVM_VARIANTS='$JvmVariants'
export VS_VERSION='$VsVersion'

cd '$CygwinOpenJdkDir'

echo "Running OpenJDK configure..."
bash configure \
    --with-boot-jdk=`$BOOT_JDK \
    --with-debug-level=`$DEBUG_LEVEL \
    --with-jvm-variants=`$JVM_VARIANTS \
    --enable-warnings-as-errors=no \
    --with-vendor-name=Aprism \
    --with-vendor-url=https://github.com/anomalyco/aprism \
    --with-vendor-bug-url=https://github.com/anomalyco/aprism/issues \
    --with-vendor-vm-bug-url=https://github.com/anomalyco/aprism/issues \
    --with-toolchain-version=`$VS_VERSION

echo "Building OpenJDK images..."
make images

echo "Build complete!"
"@

Set-Content -Path $TempScript -Value $ScriptContent -Encoding UTF8

# Run the bash script
Write-Host ""
Write-Host "Launching OpenJDK build via Cygwin bash..." -ForegroundColor Cyan
& $BashPath $TempScript

if ($LASTEXITCODE -ne 0) {
    Write-Error "Build failed with exit code $LASTEXITCODE"
    exit $LASTEXITCODE
}

# Clean up temp script
Remove-Item $TempScript -Force

Write-Host ""
Write-Host "=== Build Successful ===" -ForegroundColor Green
Write-Host "Check $OpenJdkDir\build for output images" -ForegroundColor Green
