#Requires -Version 5.1
# AprismJDK v26.3-Alpha.8 performance baseline.
# Runs identical benchmarks on stock JDK 25 and fork image, outputs table.
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

# Single-file benchmark that runs on both runtimes (no AprismJDK deps)
$benchSrc = Join-Path $env:Temp "PerfBench.java"
@'
public class PerfBench {
    public static void main(String[] args) throws Exception {
        int WARMUP = 200_000;
        int OPS    = 2_000_000;
        int ROUNDS = 5;

        // --- String concat ---
        for (int i = 0; i < WARMUP; i++) doConcat("a", i);
        long[] times = new long[ROUNDS];
        for (int r = 0; r < ROUNDS; r++) {
            long t0 = System.nanoTime();
            for (int i = 0; i < OPS / 10; i++) doConcat("key", i);
            times[r] = System.nanoTime() - t0;
        }
        report("string_concat", times, OPS / 10);

        // --- Math loop ---
        for (int i = 0; i < WARMUP; i++) doMath(i);
        for (int r = 0; r < ROUNDS; r++) {
            long t0 = System.nanoTime();
            for (int i = 0; i < OPS; i++) doMath(i);
            times[r] = System.nanoTime() - t0;
        }
        report("math_loop", times, OPS);

        // --- ArrayList ops ---
        var list = new java.util.ArrayList<Integer>(1000);
        for (int i = 0; i < WARMUP / 10; i++) list.add(i);
        for (int r = 0; r < ROUNDS; r++) {
            long t0 = System.nanoTime();
            for (int i = 0; i < OPS / 20; i++) {
                list.set(i % list.size(), i);
                if (i % 100 == 0) list.size();
            }
            times[r] = System.nanoTime() - t0;
        }
        report("arraylist_ops", times, OPS / 20);

        // --- Startup to first output ---
        System.out.println("RUNTIME=" + System.getProperty("java.vendor") + " "
            + System.getProperty("java.version"));
    }

    static String doConcat(String prefix, int n) { return prefix + "-" + n; }
    static long doMath(int n) { return (long)n * 31 + n * 17; }

    static void report(String name, long[] nanos, int ops) {
        java.util.Arrays.sort(nanos);
        double medianMs = nanos[nanos.length/2] / 1e6;
        System.out.printf("BENCH,%s,%.2f%n", name, medianMs);
    }
}
'@ | Set-Content $benchSrc -Encoding ascii

function Run-Bench([string]$label, [string]$javaExe) {
    Write-Host "--- $label ---"
    $out = & $javaExe $benchSrc 2>&1
    foreach ($line in $out) {
        if ($line -match '^BENCH,(.+),([\d.]+)$') {
            [pscustomobject]@{ Runtime=$label; Benchmark=$Matches[1]; MedianMs=[double]$Matches[2] }
        } elseif ($line -match '^RUNTIME=(.+)') {
            Write-Host "  runtime: $($Matches[1])"
        }
    }
}

Write-Host "=== AprismJDK v26.3-Alpha.8 Performance Baseline ===" -ForegroundColor Cyan

$allResults = @()
if ($StockJava -and (Test-Path "$StockJava\bin\java.exe")) {
    $allResults += Run-Bench "stock25" "$StockJava\bin\java.exe"
}
if ($ForkImage -and (Test-Path "$ForkImage\bin\java.exe")) {
    $allResults += Run-Bench "fork" "$ForkImage\bin\java.exe"
}

Write-Host ""
Write-Host "| Runtime | Benchmark | Median (ms) |"
Write-Host "|---------|-----------|-------------|"
foreach ($r in $allResults) {
    Write-Host "| $($r.Runtime) | $($r.Benchmark) | $($r.MedianMs) |"
}

# Ratio comparison
$benches = $allResults | Group-Object Benchmark
foreach ($g in $benches) {
    $stockR = $g.Group | Where-Object Runtime -eq 'stock25'
    $forkR = $g.Group | Where-Object Runtime -eq 'fork'
    if ($stockR -and $forkR) {
        $ratio = [math]::Round($forkR.MedianMs / [math]::Max($stockR.MedianMs, 0.001), 2)
        Write-Host "  $($g.Name): fork/stock = ${ratio}x"
    }
}
