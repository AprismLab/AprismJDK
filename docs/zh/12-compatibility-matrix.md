# 兼容性矩阵

## 概述

AprismJDK v26.0 将跨版本兼容性作为核心设计原则。本文档概述了在不同 Java 版本和 JDK 发行版之间的兼容性保证和测试覆盖。

## 支持的 Java 版本

| Java 版本 | 支持状态 | 说明 |
|-----------|----------|------|
| Java 25   | 主要目标 | 完整功能集，优化性能 |
| Java 21 LTS | 完全支持 | 所有 API 可用，广泛测试 |
| Java 17 LTS | 完全支持 | 向后兼容的 API 接口 |
| Java 11 LTS | 不支持 | 请使用 Java 17+ |
| Java 8      | 不支持 | 请使用 Java 17+ |

## JDK 发行版兼容性

### AprismJDK

| 功能 | v26.0-Alpha.8 | v26.0-GA（计划中） |
|------|---------------|-------------------|
| VmInfo API | ✅ 完整 | ✅ 完整 |
| 事件系统 | ✅ 完整 | ✅ 完整 |
| 模组加载 | ✅ 完整 | ✅ 完整 |
| 资源管理 | ✅ 完整 | ✅ 完整 |
| 配置系统 | ✅ 完整 | ✅ 完整 |
| 网络 API | ✅ 完整 | ✅ 完整 |
| 数据序列化 | ⏳ 计划中 | ✅ 完整 |
| 安全框架 | ⏳ 计划中 | ✅ 完整 |

### 标准 JDK（Oracle、OpenJDK、Adoptium 等）

所有 AprismJDK API 在标准 JDK 上都设计为优雅降级：

| API 类别 | 标准 JDK 上的行为 |
|----------|------------------|
| `VmInfo` | 返回系统属性，所有能力标志返回 `false` |
| `EventBus` | 基本事件分发工作（无虚拟机级优化） |
| `ModLoader` | 仅支持类路径加载 |
| `ResourcePool` | 标准池化实现 |
| `ConfigManager` | 基于文件的配置工作 |
| `PacketRegistry` | 标准序列化（无虚拟机优化） |

**关键保证**：在标准 JDK 上运行时不会抛出 `NullPointerException` 或 `UnsupportedOperationException`。所有 API 返回安全默认值。

## API 兼容性

### 二进制兼容性

AprismJDK 遵循[语义化版本](https://semver.org/)：

- **主版本**（26）：允许破坏性 API 变更
- **次版本**（0）：新功能，向后兼容
- **Alpha/Beta**：预发布版本之间 API 可能变化
- **GA（正式发布）**：稳定 API，补丁版本不包含破坏性变更

### 源代码兼容性

针对 AprismJDK v26.0-Alpha.1 编译的代码可以在 v26.0-GA 上编译而无需更改（同一主版本内）。

### 运行时兼容性

使用任何 v26.0 Alpha/Beta/GA 版本编译的 JAR 在 v26.0 系列中可以互操作。

## 语言特性支持

### Java 21 特性

| 特性 | 支持状态 | 说明 |
|------|----------|------|
| 虚拟线程 | ✅ 支持 | 完全兼容 |
| 模式匹配 | ✅ 支持 | Record 模式、switch 模式 |
| 有序集合 | ✅ 支持 | 所有集合 API |
| 字符串模板 | ✅ 支持 | Java 21 预览特性 |
| 未命名类 | ✅ 支持 | 预览特性 |

### Java 17 特性

| 特性 | 支持状态 | 说明 |
|------|----------|------|
| 密封类 | ✅ 支持 | 完全支持 |
| `instanceof` 模式匹配 | ✅ 支持 | 稳定特性 |
| Record | ✅ 支持 | 用于 API 设计 |
| 文本块 | ✅ 支持 | 随处可用 |
| Switch 表达式 | ✅ 支持 | 广泛使用 |

## 平台支持

| 操作系统 | 架构 | Java 17 | Java 21 | Java 25 |
|----------|------|---------|---------|---------|
| Windows 10/11 | x64 | ✅ | ✅ | ✅ |
| Windows 10/11 | aarch64 | ✅ | ✅ | ✅ |
| Linux (glibc 2.17+) | x64 | ✅ | ✅ | ✅ |
| Linux (glibc 2.17+) | aarch64 | ✅ | ✅ | ✅ |
| macOS 11+ | x64 | ✅ | ✅ | ✅ |
| macOS 11+ | aarch64 (Apple Silicon) | ✅ | ✅ | ✅ |

## 测试矩阵

### 按 Java 版本的测试覆盖

| 测试套件 | Java 17 | Java 21 | Java 25 |
|----------|---------|---------|---------|
| VmInfo API 测试 | ✅ 5 个测试 | ✅ 5 个测试 | ⏳ 计划中 |
| Agent 测试 | ✅ 3 个测试 | ✅ 3 个测试 | ⏳ 计划中 |
| 字节码测试 | ✅ 25 个测试 | ✅ 25 个测试 | ⏳ 计划中 |
| 堆洞察测试 | ✅ 17 个测试 | ✅ 17 个测试 | ⏳ 计划中 |
| JIT 洞察测试 | ✅ 19 个测试 | ✅ 19 个测试 | ⏳ 计划中 |
| 性能测试 | ✅ 19 个测试 | ✅ 19 个测试 | ⏳ 计划中 |
| 线程洞察测试 | ✅ 12 个测试 | ✅ 12 个测试 | ⏳ 计划中 |
| 兼容性测试 | ✅ 21 个测试 | ✅ 21 个测试 | ⏳ 计划中 |
| **总计** | **121 个测试** | **121 个测试** | **计划中** |

### 按 JDK 发行版的测试覆盖

| 测试类别 | AprismJDK | 标准 JDK | 说明 |
|----------|-----------|----------|------|
| 基础 API | ✅ 完整 | ✅ 完整 | 所有方法可调用 |
| 能力检测 | ✅ 完整 | ✅ 完整 | 标准 JDK 上返回 `false` |
| 标准 JDK 回退 | ✅ 5 个测试 | ✅ 5 个测试 | 验证安全降级 |
| 跨版本兼容性 | ✅ 11 个测试 | ✅ 11 个测试 | Java 17/21/25 互操作 |

## 版本迁移指南

### 从 Java 17 迁移到 Java 21

**无需代码更改**。AprismJDK API 完全兼容。

可选改进：
- 使用虚拟线程进行异步操作
- 利用模式匹配编写更清晰的代码
- 采用有序集合 API

### 从标准 JDK 迁移到 AprismJDK

**无需代码更改**。只需替换 JDK：

```bash
# 将 JAVA_HOME 设置为 AprismJDK
export JAVA_HOME=/path/to/aprismjdk-26.0
export PATH=$JAVA_HOME/bin:$PATH

# 验证
java -version
# 输出：AprismJDK version 26.0-Alpha.8
```

**解锁新能力**：
- 虚拟机内省 API
- 高级字节码转换
- 性能优化
- 堆和 JIT 洞察

### 在 AprismJDK 版本之间迁移

**Alpha → Alpha**：可能需要代码更改（预发布 API 演进）

**Alpha → GA**：v26.0 系列内源代码兼容

**GA → GA（补丁）**：直接替换，无需更改

**v26.0 → v26.1**：次版本升级，向后兼容

**v26.x → v27.0**：主版本，可能有破坏性变更（将记录在文档中）

## 兼容性测试

### 运行兼容性测试

```bash
# 在 Java 21 上测试
export JAVA_HOME=/path/to/jdk-21
./gradlew :aprismate-tests:test --tests "*.compatibility.*"

# 在 Java 17 上测试
export JAVA_HOME=/path/to/jdk-17
./gradlew :aprismate-tests:test --tests "*.compatibility.*"

# 测试标准 JDK 回退
./gradlew :aprismate-tests:test --tests "*StockJdkFallbackTest"
```

### 持续集成

AprismJDK 在每次提交时运行兼容性测试，覆盖：
- 3 个 Java 版本（17、21、25）
- 6 个平台（Windows/Linux/macOS × x64/aarch64）
- 2 种 JDK 类型（AprismJDK、标准 JDK）

**CI 矩阵总数**：每次提交 36 个配置

## 已知限制

### 标准 JDK 限制

在标准 JDK 上运行时，以下功能**不可用**：

- **虚拟机内省**：`VmInfo.hasVmIntrospection()` 返回 `false`
- **高级字节码转换**：仅限于 Java Agent 能力
- **堆洞察**：`HeapInsight` API 返回空/默认数据
- **JIT 洞察**：`JitInsight` API 返回空/默认数据
- **性能优化**：线程局部缓存、对象池工作但没有虚拟机优化

### Java 17 限制

Java 17 缺少 Java 21+ 中可用的某些语言特性：

- **虚拟线程**：不可用（使用平台线程）
- **switch 的模式匹配**：不可用
- **字符串模板**：不可用（使用 `String.format` 或文本块）

## 弃用策略

- **Alpha/Beta**：API 可能被移除而不发出警告
- **GA**：弃用的 API 至少保留一个主版本（例如 v26 → v27）
- **弃用通知**：移除前至少 6 个月
- **迁移指南**：为所有弃用的 API 提供

## 支持时间线

| 版本 | 发布日期 | 支持结束 | LTS |
|------|----------|---------|-----|
| v26.0-Alpha.8 | 2026-08-11 | v26.0-GA | 否 |
| v26.0-GA | 2026-Q4（计划） | 2029-Q4 | 是 |
| v26.1-GA | 2027-Q2（计划） | 2030-Q2 | 是 |
| v27.0-GA | 2028-Q1（计划） | 2031-Q1 | 是 |

**LTS（长期支持）**：3 年的安全和错误修复

## 参考资料

- [Java 版本兼容性（javadoc）](../api/jdk/aprismate/util/JavaVersion.html)
- [标准 JDK 回退测试](../../aprismate-tests/src/test/java/jdk/aprismate/test/compatibility/StockJdkFallbackTest.java)
- [跨版本测试](../../aprismate-tests/src/test/java/jdk/aprismate/test/compatibility/CrossVersionCompatibilityTest.java)
- [语义化版本规范](https://semver.org/)
