#Requires -Version 5.1
# AprismJDK release packaging (v26.2-Alpha.9).
# Bundles the built fork image into zip + tar.gz with SHA256 checksums.
param(
    [string]$ImageDir = "",
    [string]$OutDir = ""
)

$ErrorActionPreference = 'Stop'
$ProjectRoot = Split-Path -Parent $PSScriptRoot

if (-not $ImageDir) {
    $img = Get-ChildItem "$ProjectRoot\openjdk-25\build" -Directory -ErrorAction SilentlyContinue |
        ForEach-Object { Join-Path $_.FullName "images\jdk\bin\java.exe" } |
        Where-Object { Test-Path $_ }
    if (-not $img) { throw "No built image found. Run the fork build first." }
    $ImageDir = Split-Path -Parent (Split-Path -Parent @($img)[0])
}
if (-not $OutDir) { $OutDir = Join-Path $ProjectRoot "releases" }

# Version from gradle.properties
$props = Get-Content (Join-Path $ProjectRoot "gradle.properties") -Raw
$version = [regex]::Match($props, 'aprismjdkVersion=(.+)').Groups[1].Value.Trim()
$safeVersion = $version -replace '^v', ''

$name = "AprismJDK-$safeVersion-windows-x64-jdk"
$stage = Join-Path $OutDir "$name"
New-Item -ItemType Directory -Path $stage -Force | Out-Null

Write-Host "Packaging $name from $ImageDir"

# Copy image contents (robocopy fast-fail mirror without deletes)
robocopy $ImageDir $stage /E /NFL /NDL /NJH /NJS /NP | Out-Null
if ($LASTEXITCODE -ge 8) { throw "robocopy failed: $LASTEXITCODE" }

# Attach provenance file
Set-Content (Join-Path $stage "APRISMJDK_VERSION") @"
product=AprismJDK
version=$version
upstream=OpenJDK 25 (jdk-25+10)
vendor=AprismLab
url=https://github.com/AprismLab/AprismJDK
license=GPL-2.0-with-classpath-exception
"@

Push-Location $OutDir
try {
    $zip = "$name.zip"
    if (Test-Path $zip) { Remove-Item $zip -Force }
    Compress-Archive -Path $name -DestinationPath $zip -CompressionLevel Optimal

    $tgz = "$name.tar.gz"
    if (Test-Path $tgz) { Remove-Item $tgz -Force }
    & tar -czf $tgz $name
    if ($LASTEXITCODE -ne 0) { throw "tar failed" }

    # SHA-256 manifest over every artifact in this run
    $sums = Get-ChildItem -File -Filter "$name.*" |
        Where-Object { $_.Name -match '\.(zip|tar\.gz)$' } |
        ForEach-Object {
            $h = (Get-FileHash $_.FullName -Algorithm SHA256).Hash.ToLower()
            "{0}  {1}" -f $h, $_.Name
        }
    # Include a jar-only artifact for stock-JDK users
    $agentJar = Join-Path $ProjectRoot "aprismate-agent\build\libs\aprismate.jar"
    if (Test-Path $agentJar) {
        Copy-Item $agentJar ".\aprismate-$safeVersion.jar" -Force
        $h = (Get-FileHash ".\aprismate-$safeVersion.jar" -Algorithm SHA256).Hash.ToLower()
        $sums += "{0}  {1}" -f $h, "aprismate-$safeVersion.jar"
    }
    $sums | Set-Content "SHA256SUMS.txt" -Encoding ascii

    Write-Host ""
    Get-ChildItem -File | Where-Object { $_.Name -like "$name.*" -or $_.Name -eq 'SHA256SUMS.txt' -or $_.Name -like 'aprismate-*.jar' } |
        Format-Table Name, @{N='MB';E={[math]::Round($_.Length/1MB,2)}} -AutoSize
    Write-Host "Checksums:"
    Get-Content "SHA256SUMS.txt"
} finally {
    Pop-Location
}

# Housekeeping note: a custom compact runtime for end users is produced
# with jlink from this image, e.g.:
#   jlink --add-modules java.base,java.logging,jdk.crypto.ec \
#         --output my-runtime   (module list per deployment)
Write-Host "`nNOTE: standalone JRE bundles are superseded by jlink recipes (modern JDK)."
