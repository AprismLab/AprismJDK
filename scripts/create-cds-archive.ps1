#Requires -Version 5.1
# CDS archive creation tool for AprismJDK.
# Usage:
#   .\create-cds-archive.ps1 -JavaHome "C:\...\jdk" -ClassPath "build\classes" `
#       -MainClass "com.example.App" [-Output "app.jsa"]
param(
    [Parameter(Mandatory)][string]$JavaHome,
    [Parameter(Mandatory)][string]$ClassPath,
    [Parameter(Mandatory)][string]$MainClass,
    [string]$Output = "app.jsa",
    [string]$ClassList = "app.lst"
)

$ErrorActionPreference = 'Stop'
$java = Join-Path $JavaHome "bin\java.exe"
if (-not (Test-Path $java)) { throw "java.exe not found at $java" }

Write-Host "=== AprismJDK CDS Archive Creator ===" -ForegroundColor Cyan
Write-Host "Java: $java"
Write-Host "ClassPath: $ClassPath"
Write-Host "MainClass: $MainClass"

# Step 1: Dump class list
Write-Host "`n[1/2] Dumping class list..." -ForegroundColor Yellow
& $java "-XX:DumpLoadedClassList=$ClassList" "-cp" $ClassPath $MainClass 2>&1 | Out-Null
if (-not (Test-Path $ClassList)) { throw "Class list not created" }
$clsCount = (Get-Content $ClassList | Measure-Object -Line).Lines
Write-Host "  -> $clsCount classes recorded"

# Step 2: Create archive
Write-Host "[2/2] Creating shared archive..." -ForegroundColor Yellow

# Windows: -Xshare:dump rejects non-empty directories in classpath.
# If classpath is a directory, jar it first.
$effectiveCp = $ClassPath
if (Test-Path $ClassPath -PathType Container) {
    $jarFile = Join-Path (Split-Path $Output -Parent) "cds-app.jar"
    if (Test-Path $jarFile) { Remove-Item $jarFile -Force }
    # Create a thin jar from the directory
    $addCmd = "jar cf `"$jarFile`" -C `"$ClassPath`" ."
    $jarExe = Join-Path $JavaHome "bin\jar.exe"
    if (Test-Path $jarExe) {
        & $jarExe cf $jarFile "-C" $ClassPath "." 2>&1 | Out-Null
        if (Test-Path $jarFile) {
            $effectiveCp = $jarFile
            Write-Host "  (directory classpath jarred to $jarFile)"
        }
    }
}

& $java "-Xshare:dump" "-XX:SharedClassListFile=$ClassList" "-XX:SharedArchiveFile=$Output" "-cp" $effectiveCp "-version" 2>&1 | Out-Null

if (Test-Path $Output) {
    $size = [math]::Round((Get-Item $Output).Length / 1MB, 2)
    Write-Host "  -> $Output created ($size MB)" -ForegroundColor Green
    Write-Host "`nUse with: java -XX:SharedArchiveFile=$Output -cp $ClassPath $MainClass"
} else {
    Write-Host "  -> archive creation failed" -ForegroundColor Red
    exit 1
}
