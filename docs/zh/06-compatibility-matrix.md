# 兼容性矩阵

> AprismJDK 子项目文档。定义跨 Java 版本兼容性契约和标准 JDK 回退行为。
>
> 作者：BlockConnect@StarsailsClover。双语：EN（权威）+ ZH（镜像）。
> v26.0-Alpha.1 交付（设计），v26.1-Alpha.8 验证。

---

## 1. 概述

AprismJDK 承诺**极致兼容性**：针对一个 AprismJDK 线构建的模组在更新后继续运行。这通过以下方式实现：

1. **基于能力的检测** — 模组查询可用内容，永不假设
2. **标准 JDK 回退** — 每个 API 都有记录的等效方案或无操作
3. **LTS 变基** — AprismJDK 跟踪上游 LTS 线，使用稳定的补丁集
4. **语义版本控制** — 破坏性变更需要主版本号提升

---

## 2. 支持的 Java 版本

AprismJDK v26.x 支持三个 Java 版本，具有分层能力：

### 2.1 Java 25（主要目标）

- **状态：** 主要开发目标
- **OpenJDK 基础：** OpenJDK 25 LTS（GA 2025-09，支持至 ~2032）
- **AprismJDK 能力：** 完整（所有功能可用）
- **平台覆盖：** Windows、Linux、macOS（x64 + aarch64）

**完整功能集：**
- AprismateAgent（premain/agentmain/自动加载）
- ClassRedefiner+（结构性重定义）
- MethodHookRegistry+（JIT 安全钩子）
- BytecodeTransformer（加载时织入）
- VmIntrospection（ThreadInsight、HeapInsight、JitInsight）
- FFM API 支持（JDK 22+ 中最终版）

### 2.2 Java 21（次要目标）

- **状态：** 前一个 LTS，通过反向移植完全支持
- **OpenJDK 基础：** OpenJDK 21 LTS（GA 2023-09，支持至 2029）
- **AprismJDK 能力：** 完整（从 Java 25 反向移植）
- **平台覆盖：** Windows、Linux、macOS

**功能集：**
- 所有 Java 25 能力已反向移植
- FFM API 可用（JDK 21 中预览，使用稳定 API）
- 与 Java 25 变体相同的 API 表面

**反向移植策略：**
- AprismJDK 补丁应用于 OpenJDK 21 基线
- `jdk.aprismate` 模块为 Java 21 目标编译
- Agent jar 使用 `-release 21` 以保持兼容性

### 2.3 Java 17（维护目标）

- **状态：** 扩展 LTS，有限支持
- **OpenJDK 基础：** OpenJDK 17 LTS（GA 2021-09，支持至 2029）
- **AprismJDK 能力：** 子集（FFM 限制）
- **平台覆盖：** Windows、Linux、macOS

**有限功能集：**
- AprismateAgent（仅 premain/agentmain，无自动加载）
- ClassRedefiner+（基本支持，有限的结构性变化）
- MethodHookRegistry+（钩子注册表工作，JIT 集成有限）
- BytecodeTransformer（完全支持）
- VmIntrospection（仅 ThreadInsight、HeapInsight；JitInsight 有限）
- 无 FFM API（Java 17 中不可用）

**优雅降级：**
- 依赖 FFM 的功能从能力查询返回 `null` 或 `false`
- 模组通过 `VmInfo.hasXxx()` 方法检测缺失

---

## 3. 标准 JDK 回退行为

每个 AprismJDK 特定 API 都记录标准 OpenJDK 等效方案：

| AprismJDK API | 标准 JDK 回退 | 注释 |
|---------------|----------------|------|
| `VmInfo.isAprismJdk()` | 返回 `false` | 检测方法 |
| `VmInfo.hasClassRedefinerPlus()` | 返回 `false` | 使用标准 `Instrumentation` |
| `Agent.getClassRedefiner()` | 返回 `null` | 使用 `Instrumentation.redefineClasses()` |
| `Agent.getMethodHookRegistry()` | 返回 `null` | 使用 ASM 方法包装 |
| `Agent.getBytecodeTransformer()` | 返回 `null` | 使用 `ClassFileTransformer` |
| `ThreadInsight.getAllThreads()` | 使用 `ThreadMXBean` | `ManagementFactory.getThreadMXBean()` |
| `HeapInsight.getHeapSummary()` | 使用 `MemoryMXBean` | `ManagementFactory.getMemoryMXBean()` |
| `JitInsight.getCompiledMethods()` | 使用 `CompilationMXBean` | 可用信息有限 |

### 回退示例

```java
// 检测 AprismJDK 并使用适当的 API
ClassRedefiner redefiner;
if (VmInfo.isAprismJdk() && VmInfo.hasClassRedefinerPlus()) {
    redefiner = (ClassRedefiner) Agent.getClassRedefiner();
} else {
    // 回退：使用标准 Instrumentation
    redefiner = new StandardRedefinerWrapper(instrumentation);
}
```

---

## 4. 前向兼容契约

### 4.1 能力描述符稳定性

一旦能力被标记为**稳定**（GA 版本），它遵循语义版本控制：

- **次要更新**（v26.1 → v26.2）— 仅累加，无破坏性变更
- **主要更新**（v26.x → v27.x）— 可能弃用旧能力并提供迁移路径
- **弃用通知** — 至少 2 个次要版本后才删除

### 4.2 API 演进

```java
// v26.1 GA - 初始稳定版本
public static ThreadSnapshot[] getAllThreads()

// v26.2 - 累加：新重载（兼容）
public static ThreadSnapshot[] getAllThreads(ThreadFilter filter)

// v26.5 - 弃用通知
@Deprecated(since="v26.5", forRemoval=true)
public static ThreadSnapshot[] getAllThreads()

// v27.0 - 弃用期后允许删除
// 方法删除，引入新 API
```

### 4.3 跨版本测试

AprismJDK 发布针对以下内容测试：

1. **上游 LTS 更新** — OpenJDK 25.0.1、25.0.2 等
2. **先前的 AprismJDK 版本** — 验证二进制兼容性
3. **标准 OpenJDK** — 在标准 25、21、17 上验证回退行为

测试矩阵：
- v26.1 GA → OpenJDK 25.0.0、25.0.1、25.0.2
- v26.1 GA → AprismJDK 21 变体
- v26.1 GA → AprismJDK 17 变体  
- v26.1 GA → 标准 OpenJDK 25、21、17

---

## 5. LTS 变基策略

AprismJDK 跟踪上游 OpenJDK LTS 版本：

### 当前状态（v26.x）
- **基础：** OpenJDK 25 LTS
- **补丁集：** AprismJDK v26 补丁
- **支持：** 直到 OpenJDK 26 LTS（估计 2028）

### 未来 LTS 过渡（v2X.x）
当上游发布下一个 LTS（例如 2028 年的 OpenJDK 28）：

1. **变基：** 将 AprismJDK 补丁集应用于 OpenJDK 28 基线
2. **测试：** 在新基础上验证所有能力
3. **发布：** 新主线（v28.0-Alpha.1...）
4. **维护：** 在重叠期继续 v26.x 安全更新

### 重叠期

在 LTS 过渡期间，两条线都接收更新：
- **v26.x** — 仅安全修复（1 年重叠）
- **v28.x** — 活跃功能开发

---

## 6. 平台特定兼容性

### 6.1 Windows

- **支持：** Windows 10 1809+、Windows 11、Windows Server 2019+
- **架构：** x64（主要）、ARM64（未来）
- **工具链：** MSVC 2022（用于 v26.1+ 中的本地组件）

### 6.2 Linux

- **支持：** glibc 2.27+（Ubuntu 18.04+、RHEL 8+、Debian 10+）
- **架构：** x64（主要）、aarch64（次要）
- **工具链：** GCC 11+ 或 Clang 14+

### 6.3 macOS

- **支持：** macOS 12（Monterey）+
- **架构：** x64、aarch64（Apple Silicon）
- **工具链：** Xcode 14+

### 6.4 不支持的平台

- **Android/iOS：** 不适用（无桌面 JDK）
- **游戏机：** 不支持（无 Java 运行时）
- **嵌入式：** 未测试（使用标准 OpenJDK）

---

## 7. 二进制兼容性

### 7.1 JAR 兼容性

针对一个 AprismJDK 版本编译的 jar 在任何其他 v26.x 版本上运行：

```
使用 AprismJDK v26.0 GA 编译的模组
→ 在 AprismJDK v26.1、v26.2、... v26.9 上运行
→ 在标准 OpenJDK 25/21/17 上运行（带回退）
```

### 7.2 模块兼容性

`jdk.aprismate` 模块仅累加：
- **v26.0 GA：** 基本 VmInfo、Agent 桩
- **v26.1 GA：** + 运行时 API（ThreadInsight、HeapInsight、JitInsight）
- **v26.2 GA：** + 性能 API（CpuFeatures、CacheTopology）
- **v26.3 GA：** + FFM 桥接（Cpp2Java、Rust2Java）

使用 v26.0 API 的代码在 v26.3 上编译和运行不变。

---

## 8. 依赖兼容性

### 8.1 ASM 库

AprismateAgent 嵌入 ASM 库（重定位）：
- **嵌入版本：** ASM 9.7+
- **重定位：** `org.objectweb.asm` → `com.aprismate.agent.shadow.asm`
- **隔离：** 与应用程序 ASM 依赖无冲突

### 8.2 JNI/本地库

本地组件（v26.1+）遵循平台 ABI：
- **Windows：** MSVC 2022 运行时，C++17
- **Linux：** glibc 2.27+，GCC 11+ libstdc++
- **macOS：** macOS 12+ SDK，Clang 14+

---

## 9. 升级路径

### 从标准 OpenJDK 到 AprismJDK

1. 替换 JDK 安装目录
2. 更新 `JAVA_HOME` 环境变量
3. 无需代码更改（累加 API）
4. 可选：使用 AprismJDK 特定能力

### 从 AprismJDK v26.0 到 v26.1

1. 替换 JDK/JRE 发行版
2. 无需重新编译（二进制兼容）
3. 通过 `VmInfo.hasXxx()` 查询可用新能力

### 从 AprismJDK v26.x 到 v27.x（未来）

1. 审查 v26.x 版本中的弃用通知
2. 在 v27.0 之前迁移到新 API
3. 建议重新编译（主版本边界）

---

## 10. 测试矩阵（v26.1 GA 目标）

| 测试维度 | 覆盖范围 |
|----------|----------|
| **Java 版本** | OpenJDK 25、21、17（所有 AprismJDK 变体 + 标准） |
| **平台** | Windows x64、Linux x64、macOS x64/aarch64 |
| **能力** | 所有 API，带/不带 AprismJDK 功能 |
| **回退** | 所有 API 的标准 JDK 行为 |
| **兼容性** | 跨版本二进制兼容性 |

**总测试场景：** v26.1 GA 时约 230 个测试

---

## 11. 运行时版本检测

跨版本代码的推荐模式：

```java
public class AprismJdkCompat {
    private static final boolean IS_APRISMJDK = detectAprismJdk();
    private static final int JAVA_VERSION = Runtime.version().feature();
    
    private static boolean detectAprismJdk() {
        try {
            Class.forName("jdk.aprismate.VmInfo");
            return jdk.aprismate.VmInfo.isAprismJdk();
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    public static boolean canUseDeepRedefine() {
        return IS_APRISMJDK 
            && JAVA_VERSION >= 21
            && jdk.aprismate.VmInfo.hasClassRedefinerPlus();
    }
}
```

---

## 12. 参考

- JEP 223 — 新版本字符串方案（版本检测）
- JEP 403 — 强封装 JDK 内部（稳定 API 的动机）
- 语义版本控制 2.0.0 — 版本控制契约
- OpenJDK LTS 路线图 — 上游跟踪策略
