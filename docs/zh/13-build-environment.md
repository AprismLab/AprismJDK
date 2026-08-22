# AprismJDK 构建环境参考

> AprismJDK 用于构建的工具链权威记录。更新于 FACT.md Session 11
> （2026-08-18）。中文镜像；英文正本见 docs/13-build-environment.md。

---

## 1. 宿主机基线（v26.2-Alpha.1）

| 组件 | 值 | 备注 |
|---|---|---|
| 操作系统 | Windows 11 x64（build 10.0.28020） | 主要开发机 |
| CPU / 内存 | 20 核 / 16 GB | fork 构建时 `make JOBS=15` 上限 |
| 清理后磁盘可用 | ~23.6 GB | MC 工具链缓存（~8.5 GB）有意保留 |
| 系统 Java | Temurin 21.0.11+10-LTS | PATH 默认 `java` |
| 目标 Java | Temurin **25.0.3+9-LTS**，位于 `C:\Users\Sails\Java\jdk-25.0.3+9` | Gradle daemon 与全部模块的 toolchain |

## 2. JVM 构建（Gradle 侧）

| 项目 | 版本 | 原因 |
|---|---|---|
| Gradle wrapper | **9.7.0-bin**（腾讯镜像） | 8.5 无法在 JDK 25 上运行；镜像与兄弟项目一致且本地已有发行包缓存 |
| Toolchain | Java **25**（根 build.gradle） | FFM（`java.lang.foreign`）自 22 起转正，无需 preview 标志 |
| junit-jupiter | 5.10.2（显式添加 platform launcher 1.10.2） | Gradle 9 不再自动注入 launcher |
| Mockito | **5.23.0** | <=5.11 的 ByteBuddy 无法插桩 JDK 25 类（"Could not modify all classes"） |
| ASM | 9.7.1 | 未变 |
| Javadoc | `-Xdoclint:none -quiet` | JDK 25 javadoc 默认启用 doclint，遗留 stub 无法通过 HTML 检查 |

### 复现 JVM 构建

```powershell
$env:JAVA_HOME = "C:\Users\Sails\Java\jdk-25.0.3+9"
.\gradlew.bat build            # 编译 + 全量测试
# 2026-08-18 验证：647 tests, 0 failures, 3 skipped
```

## 3. 原生 Fork 构建状态（v26.2-Alpha.2 目标）

原生 OpenJDK 构建**在 MSYS2 上被阻塞**，将迁移至 WSL2：

1. MSYS2 make 报告 `Built for x86_64-pc-cygwin`；OpenJDK configure
   （msys2 路径）要求 `msys` —— 被拒绝。
2. 强制 cygwin 检测会破坏 TEMP 提取（`-t` 参数收到 cmd.exe 横幅文本），
   导致 fixpath 生成失败。
3. fixpath.sh 反斜杠在 bash `-c` 字符串中被吞掉
   （`C:\msys64\usr\bin\bash` → `C:msys64usrbinbash`）。
4. fixpath atfile 的 EXIT trap 竞态会在 jar.exe 读取前删除参数文件。

完整复盘：`.trae/sessions.md`（本地，不入库）；摘要见 FACT.md
Session 11。该会话对源码树的全部补丁已还原（basic_windows.m4、
toolchain_microsoft.m4、fixpath.sh 已 `git checkout`，工作树干净）。

### WSL2 计划（Alpha.2）

- 安装 WSL2 + Ubuntu LTS
- `sudo apt install build-essential autoconf zip unzip`
  （boot JDK 24/25）
- 复用 `openjdk-25/` 源码树（干净 checkout，tag jdk-25+10）
- 保留 Session 10 尝试的 configure 标志：
  `--with-vendor-name=AprismLab --with-version-string=26.2.1 ...`
  （去掉 Windows 专用的 `--with-toolchain-version`）
- 就绪脚本：`scripts/setup-wsl2.ps1`（提权一次性安装）→ 重启 →
  `scripts/wsl2-builddeps.sh` + `scripts/build-openjdk-wsl.sh`

## 4. Minecraft 测试装置（v26.3 目标）

MDL（MCDebugLauncher）v26.1.0 已在 PATH 中。AJR 验证启动的关键能力：

```bash
mdl launch <name> --java-path C:\path\to\AprismJDK-image   # 用 AJR 启动
mdl launch <name> --aprism                                  # 附着 Aprism loader agent
mdl logs <name> --level error                               # 问题定位
```

现有实例覆盖 MC 26.x vanilla/fabric/neoforge 矩阵（62 个实例）。
fabric/quilt 线可用 Despotes 控制平面做游戏内断言与性能对比。

## 5. 已知约束

- **磁盘**：任何原生 fork 构建前保持 >15 GB 可用；清理
  `ng_execute`/`forge_gradle` 缓存（合计 ~8.5 GB）需用户确认
  （活跃 MC 项目重新下载很慢）。
- **网络**：gradle.org 直连超时；腾讯镜像是可靠路径。
  Maven Central 可直连。
- **签名**：发布要求 SSH 签名的 commit/tag（见 README §Governance）。
  GA 打 tag 前确认 `git config gpg.format ssh`。
