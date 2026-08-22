# OpenJDK Build Wrapper for AprismJDK (Windows PowerShell)
# Uses MSYS2 for POSIX toolchain and MSVC for native compiler

param(
    [string]$BootJdk = "",
    [string]$DebugLevel = "release",
    [string]$VsVersion = "",  # Auto-detected if empty; override for specific versions
    [string]$Target = "configure",
    [string]$ConfigureArgs = ""
)

$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptDir
$OpenJdkDir = Join-Path $ProjectRoot "openjdk-25"
$Msys2Bash = "C:\msys64\usr\bin\bash.exe"

if (-not $BootJdk) {
    $BootJdk = $env:JAVA_HOME
}
if (-not $BootJdk) {
    $BootJdk = "C:\Users\Sails\AppData\Local\Programs\jdk-24-boot\jdk-24"
}

Write-Host "=== AprismJDK OpenJDK Build Wrapper ===" -ForegroundColor Cyan
Write-Host "Platform: Windows (MSYS2 + MSVC)"
Write-Host "OpenJDK Directory: $OpenJdkDir"
Write-Host "Boot JDK: $BootJdk"
Write-Host "Debug Level: $DebugLevel"
Write-Host "Target: $Target"
Write-Host "========================================" -ForegroundColor Cyan

if (-not (Test-Path $OpenJdkDir)) {
    Write-Error "OpenJDK source directory not found at $OpenJdkDir"
    exit 1
}

if (-not (Test-Path $BootJdk)) {
    Write-Error "Boot JDK not found at $BootJdk"
    exit 1
}

if (-not (Test-Path $Msys2Bash)) {
    Write-Error "MSYS2 not found at $Msys2Bash. Install MSYS2 first."
    exit 1
}

# Convert Windows paths to MSYS2 paths
function ConvertTo-Msys2Path([string]$winPath) {
    $drive = $winPath.Substring(0, 1).ToLower()
    $rest = $winPath.Substring(2) -replace '\\', '/'
    return "/$drive$rest"
}

$MsysBootJdk = ConvertTo-Msys2Path $BootJdk
$MsysOpenJdkDir = ConvertTo-Msys2Path $OpenJdkDir
$MsysProjectRoot = ConvertTo-Msys2Path $ProjectRoot

# Find MSVC vcvars64.bat
$VsWhere = "${env:ProgramFiles(x86)}\Microsoft Visual Studio\Installer\vswhere.exe"
if (Test-Path $VsWhere) {
    $VsInstallPath = & $VsWhere -latest -property installationPath
    $VcVarsBat = Join-Path $VsInstallPath "VC\Auxiliary\Build\vcvars64.bat"
    Write-Host "MSVC vcvars64: $VcVarsBat" -ForegroundColor Green

    # Auto-detect VS version if not specified
    if (-not $VsVersion) {
        $VsProductLine = & $VsWhere -latest -property catalog_productLineVersion
        $VsVersion = $VsProductLine
        Write-Host "Auto-detected VS product line: $VsVersion" -ForegroundColor Green
    }
} else {
    Write-Error "Visual Studio vswhere not found"
    exit 1
}

# Create a temporary bash script using single-quoted here-string (no PS expansion)
$TempScript = Join-Path $env:TEMP "aprism-build-jdk.sh"

# Build the bash script content
$bashLines = @(
    '#!/bin/bash',
    'set -ex',
    '',
    '# Set locale to avoid cmd.exe output corruption',
    'export LANG=C',
    'export LC_ALL=C',
    'export MSYS2_ARG_CONV_EXCL="*"',
    "export BOOT_JDK='$MsysBootJdk'",
    "export DEBUG_LEVEL='$DebugLevel'",
    "export VS_VERSION='$VsVersion'",
    '',
    "cd '$MsysOpenJdkDir'",
    ''
)

switch ($Target) {
    "configure" {
        $bashLines += @(
            'echo "Running OpenJDK configure..."',
            'bash configure \',
            '    "--with-boot-jdk=$BOOT_JDK" \',
            '    --with-debug-level=$DEBUG_LEVEL \',
            '    --with-jvm-variants=server \',
            '    --enable-warnings-as-errors=no \',
            '    --with-vendor-name=AprismLab \',
            '    --with-vendor-url=https://github.com/AprismLab/AprismJDK \',
            '    --with-vendor-bug-url=https://github.com/AprismLab/AprismJDK/issues \',
            '    --with-vendor-vm-bug-url=https://github.com/AprismLab/AprismJDK/issues \',
            '    --with-version-opt=AprismJDK \',
            '    --with-version-build=1'
        )
        if ($ConfigureArgs) {
            $bashLines += "    $ConfigureArgs"
        }
        $bashLines += @(
            '',
            'echo "Configure complete!"'
        )
    }
    "make" {
        $bashLines += @(
            'echo "Building OpenJDK images..."',
            'make images CONF=*',
            '',
            'echo "Build complete!"'
        )
    }
    "configure-make" {
        $bashLines += @(
            'echo "Running OpenJDK configure..."',
            'bash configure \',
            '    "--with-boot-jdk=$BOOT_JDK" \',
            '    --with-debug-level=$DEBUG_LEVEL \',
            '    --with-jvm-variants=server \',
            '    --enable-warnings-as-errors=no \',
            '    --with-vendor-name=AprismLab \',
            '    --with-vendor-url=https://github.com/AprismLab/AprismJDK \',
            '    --with-vendor-bug-url=https://github.com/AprismLab/AprismJDK/issues \',
            '    --with-vendor-vm-bug-url=https://github.com/AprismLab/AprismJDK/issues \',
            '    --with-version-opt=AprismJDK \',
            '    --with-version-build=1'
        )
        if ($ConfigureArgs) {
            $bashLines += "    $ConfigureArgs"
        }
        $bashLines += @(
            '',
            'echo "Building OpenJDK images..."',
            'make images CONF=*',
            '',
            'echo "Build complete!"'
        )
    }
    "clean" {
        $bashLines += @(
            'echo "Cleaning build..."',
            'make clean',
            'echo "Clean complete!"'
        )
    }
    default {
        Write-Error "Unknown target: $Target"
        exit 1
    }
}

$ScriptContent = $bashLines -join "`n"
[System.IO.File]::WriteAllText($TempScript, $ScriptContent, [System.Text.UTF8Encoding]::new($false))

$MsysTempScript = ConvertTo-Msys2Path $TempScript

Write-Host ""
Write-Host "Launching build via MSYS2 bash..." -ForegroundColor Cyan
Write-Host "Script: $TempScript (MSYS2: $MsysTempScript)"
& $Msys2Bash -lc "bash '$MsysTempScript'"

$exitCode = $LASTEXITCODE
Remove-Item $TempScript -Force -ErrorAction SilentlyContinue

if ($exitCode -ne 0) {
    Write-Error "Build failed with exit code $exitCode"
    exit $exitCode
}

Write-Host ""
Write-Host "=== Build Successful ===" -ForegroundColor Green
Write-Host "Check $OpenJdkDir\build for output images" -ForegroundColor Green
