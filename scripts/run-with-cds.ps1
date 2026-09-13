#Requires -Version 5.1
# Run an app with a CDS archive (or auto-create on first run).
# Usage:
#   .\run-with-cds.ps1 -JavaHome "C:\...\jdk" -ClassPath "build\classes" `
#       -MainClass "com.example.App" [-Archive "app.jsa"] [-AutoCreate]
param(
    [Parameter(Mandatory)][string]$JavaHome,
    [Parameter(Mandatory)][string]$ClassPath,
    [Parameter(Mandatory)][string]$MainClass,
    [string]$Archive = "app.jsa",
    [switch]$AutoCreate
)

$ErrorActionPreference = 'Stop'
$java = Join-Path $JavaHome "bin\java.exe"
if (-not (Test-Path $java)) { throw "java.exe not found at $java" }

if ($AutoCreate) {
    Write-Host "Auto-creating CDS archive on first run..."
    & $java "-XX:+AutoCreateSharedArchive" "-XX:SharedArchiveFile=$Archive" "-cp" $ClassPath $MainClass @args
} elseif (Test-Path $Archive) {
    Write-Host "Running with CDS archive: $Archive"
    $t0 = [System.Diagnostics.Stopwatch]::StartNew()
    & $java "-XX:SharedArchiveFile=$Archive" "-cp" $ClassPath $MainClass @args
    $t0.Stop()
    Write-Host "startup: $($t0.ElapsedMilliseconds)ms"
} else {
    Write-Host "No archive found, running without CDS"
    & $java "-cp" $ClassPath $MainClass @args
}
