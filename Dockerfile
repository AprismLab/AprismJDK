# AprismJDK container image
# Multi-stage: builds the fork from source, packages into minimal runtime.
# Build: docker build -t aprismjdk:26.5 .
# Run:   docker run --rm aprismjdk:26.5 java -version
FROM ubuntu:22.04 AS build
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential autoconf zip unzip file wget git ca-certificates \
    libx11-dev libxext-dev libxrender-dev libxrandr-dev libxtst-dev libxt-dev \
    libcups2-dev libfontconfig1-dev libasound2-dev libfreetype6-dev libffi-dev \
    && rm -rf /var/lib/apt/lists/*
RUN wget -q -O /tmp/bootjdk.tar.gz \
    "https://api.adoptium.net/v3/binary/latest/25/ga/linux/x64/jdk/hotspot/normal/eclipse" \
    && mkdir -p /opt/bootjdk \
    && tar -xzf /tmp/bootjdk.tar.gz -C /opt/bootjdk --strip-components=1
ENV JAVA_HOME=/opt/bootjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"
WORKDIR /build
RUN git clone --depth 1 --branch jdk-25+10 https://github.com/openjdk/jdk.git openjdk-25
COPY . /build/AprismJDK
RUN cd /build/openjdk-25 && bash ../AprismJDK/scripts/apply-patches.sh \
    && mkdir -p lib \
    && cd /build/AprismJDK \
    && ./gradlew :aprismate-agent:embedJar --no-daemon 2>/dev/null; \
    cp aprismate-agent/build/libs/aprismate.jar /build/openjdk-25/lib/ 2>/dev/null; exit 0
RUN cd /build/openjdk-25 && bash configure \
    --with-boot-jdk=/opt/bootjdk --with-debug-level=release \
    --with-jvm-variants=server --enable-warnings-as-errors=no \
    --disable-javac-server --with-vendor-name=AprismLab \
    --with-vendor-version-string=AJR \
    --with-vendor-url=https://github.com/AprismLab/AprismJDK \
    --with-vendor-bug-url=https://github.com/AprismLab/AprismJDK/issues \
    --with-vendor-vm-bug-url=https://github.com/AprismLab/AprismJDK/issues \
    --with-version-string=25.2.1 \
    && for i in 1 2 3; do make images JOBS=$(nproc) LOG=info && break || true; done

FROM ubuntu:22.04
RUN apt-get update && apt-get install -y --no-install-recommends ca-certificates \
    && rm -rf /var/lib/apt/lists/*
COPY --from=build /build/openjdk-25/build/*/images/jdk /opt/aprismjdk
ENV JAVA_HOME=/opt/aprismjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"
RUN java -version && java --list-modules | grep jdk.aprismate
LABEL org.opencontainers.image.title="AprismJDK"
LABEL org.opencontainers.image.description="OpenJDK 25 fork with jdk.aprismate module"
LABEL org.opencontainers.image.vendor="AprismLab"
LABEL org.opencontainers.image.source="https://github.com/AprismLab/AprismJDK"
