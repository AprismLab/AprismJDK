#Requires -Version 5.1
# AprismJDK startup benchmark for CI.
# Runs StartupBenchmark on both runtimes, outputs comparison table.
# Exit code: 1 if fork startup > 2x stock (regression).
param(
    [string]$ForkImage = "",
    [string]$StockJava = ""
)

$ErrorActionPreference = 'Continue'
$ProjectRoot = Split-Path -Parent $PSScriptRoot

if (-not $ForkImage) {
    $img = Get-ChildItem "$ProjectRoot\openjdk-25\build" -Directory -ErrorAction SilentlyContinue |
        ForEach-Object { Join-Path $_.FullName "images\jdk\bin\java.exe" } |
        Where-Object { Test-Path $_ }
    if ($img) { $ForkImage = Split-Path -Parent (Split-Path -Parent @($img)[0]) }
}
if (-not $StockJava) {
    foreach ($c in @("C:\Users\Sails\Java\jdk-25.0.3+9",
                     "C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot")) {
        if (Test-Path "$c\bin\java.exe") { $StockJava = $c; break }
    }
}

# Single-file source-launcher mode works on both runtimes (no module conflicts)
$benchSrc = Join-Path $env:Temp "StartupBench.java"
@'
import java.lang.management.ManagementFactory;
public class StartupBench {
    public static void main(String[] args) {
        var rt = ManagementFactory.getRuntimeMXBean();
        var mem = ManagementFactory.getMemoryMXBean();
        var cl = ManagementFactory.getClassLoadingMXBean();
        var th = ManagementFactory.getThreadMXBean();
        var list = new java.util.ArrayList<String>(500);
        for (int i = 0; i < 500; i++) list.add("item" + i);
        System.out.println("total_uptime_ms," + rt.getUptime());
        System.out.println("loaded_classes," + cl.getLoadedClassCount());
        System.out.println("runtime," + System.getProperty("java.vendor"));
    }
}
'@ | Set-Content $benchSrc -Encoding ascii

function Run-Bench([string]$label, [string]$javaExe, [int]$iterations = 10) {
    $allTimes = @()
    for ($i = 0; $i -lt $iterations; $i++) {
        $out = & $javaExe $benchSrc 2>&1 | Out-String
        $uptime = [regex]::Match($out, 'total_uptime_ms,(\d+)').Groups[1].Value
        if ($uptime) { $allTimes += [int]$uptime }
    }
    if ($allTimes.Count -eq 0) { return $null }
    $sorted = $allTimes | Sort-Object
    $median = $sorted[[int]($sorted.Count / 2)]
    [pscustomobject]@{ Runtime=$label; Median=$median; Min=$sorted[0]; Max=$sorted[-1]; Times=($allTimes -join ',') }
}

Write-Host "=== AprismJDK Startup Benchmark (v26.5-Alpha.3) ===" -ForegroundColor Cyan

$results = @()
if ($StockJava -and (Test-Path "$StockJava\bin\java.exe")) {
    $results += Run-Bench "stock25" "$StockJava\bin\java.exe"
}
if ($ForkImage -and (Test-Path "$ForkImage\bin\java.exe")) {
    $results += Run-Bench "fork" "$ForkImage\bin\java.exe"
}

Write-Host ""
Write-Host "| Runtime | Median (ms) | Min | Max |"
Write-Host "|---------|-------------|-----|-----|"
foreach ($r in $results) {
    Write-Host "| $($r.Runtime) | $($r.Median) | $($r.Min) | $($r.Max) |"
}

# Regression check
$stockR = $results | Where-Object Runtime -eq 'stock25'
$forkR = $results | Where-Object Runtime -eq 'fork'
if ($stockR -and $forkR) {
    $ratio = [math]::Round($forkR.Median / [math]::Max($stockR.Median, 1), 2)
    Write-Host "`n  fork/stock ratio: ${ratio}x"
    if ($ratio -gt 2.0) {
        Write-Host "  REGRESSION: fork startup > 2x stock!" -ForegroundColor Red
        exit 1
    }
    Write-Host "  OK (within 2x threshold)" -ForegroundColor Green
}
