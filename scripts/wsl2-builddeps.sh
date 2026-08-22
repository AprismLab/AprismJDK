#!/usr/bin/env bash
# AprismJDK fork-build dependencies inside WSL2 Ubuntu.
# Usage: wsl -d Ubuntu -e bash scripts/wsl2-builddeps.sh
set -euo pipefail

sudo apt-get update
sudo apt-get install -y --no-install-recommends \
  build-essential autoconf zip unzip file wget \
  libx11-dev libxext-dev libxrender-dev libxrandr-dev libxtst-dev libxt-dev \
  libcups2-dev libfontconfig1-dev libasound2-dev libfreetype6-dev \
  libffi-dev

# Boot JDK for building OpenJDK 25 (needs N-1: 24, or 25 itself)
if [ ! -d "$HOME/bootjdk" ]; then
  echo "Downloading boot JDK 25..."
  mkdir -p "$HOME/bootjdk"
  wget -q -O /tmp/bootjdk.tar.gz \
    "https://mirrors.tuna.tsinghua.edu.cn/Adoptium/24/jdk/x64/linux/OpenJDK24U-jdk_x64_linux_hotspot_24.0.2_12.tar.gz" \
    || wget -q -O /tmp/bootjdk.tar.gz \
    "https://api.adoptium.net/v3/binary/latest/24/ga/linux/x64/jdk/hotspot/normal/eclipse"
  tar -xzf /tmp/bootjdk.tar.gz -C "$HOME/bootjdk" --strip-components=1
fi
"$HOME/bootjdk/bin/java" -version

echo "WSL2 build deps OK."
