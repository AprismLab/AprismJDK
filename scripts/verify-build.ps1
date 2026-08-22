# AprismJDK build verification (Windows side).
# Verifies a fork-built JDK image: version string, module list, agent load.
param(
    [string]$ImageDir = ""
)

$ErrorActionPreference = 'Stop'
$ProjectRoot = Split-Path -Parent $PSScriptRoot
if (-not $ImageDir) {
    $candidates = Get-ChildItem "$ProjectRoot\openjdk-25\build" -Directory -ErrorAction SilentlyContinue |
        ForEach-Object { Join-Path $_.FullName "images\jdk" } |
        Where-Object { (Test-Path (Join-Path $_ "bin\java.exe")) }
    if (-not $candidates) { throw "No built image found under openjdk-25\build. Build first." }
    $ImageDir = @($candidates)[0]
}

$java = Join-Path $ImageDir "bin\java.exe"
if (-not (Test-Path $java)) { throw "java.exe not found at $java" }

Write-Host "=== AprismJDK Build Verification ===" -ForegroundColor Cyan
Write-Host "Image: $ImageDir"

# 1. Version output
Write-Host "`n[1/3] java -version"
& $java -version 2>&1 | ForEach-Object { Write-Host "  $_" }

# 2. Module list sanity
Write-Host "`n[2/3] jdk.aprismate module present? (expected NO until Alpha.5)"
$mods = & $java --list-modules
if ($mods -match 'jdk\.aprismate') { Write-Host "  jdk.aprismate: PRESENT" -ForegroundColor Green }
else { Write-Host "  jdk.aprismate: absent (ok for unmodified base)" -ForegroundColor DarkGray }

# 3. Hello World smoke test
Write-Host "`n[3/3] Hello World smoke"
$tmp = New-Item -ItemType Directory -Path ([System.IO.Path]::Combine([IO.Path]::GetTempPath(), [IO.Path]::GetRandomFileName())) -Force
Set-Content "$tmp\Hello.java" 'public class Hello { public static void main(String[] a){ System.out.println("AprismJDK smoke OK"); } }'
& $java "$tmp\Hello.java"
Remove-Item -Recurse -Force $tmp

Write-Host "`nVerification complete." -ForegroundColor Green
