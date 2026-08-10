# 开放接口与能力

> AprismJDK 子项目文档。描述 `jdk.aprismate` API 模块：公开 JVM 内部的稳定、版本化接口。
>
> 作者：BlockConnect@StarsailsClover。双语：EN（权威）+ ZH（镜像）。
> v26.0-Alpha.1 交付（设计），跨 v26.0-v26.1 线实现。

---

## 1. 概述

AprismJDK 将常用的 JVM 内部提升为**稳定的、版本化的 API 模块**（`jdk.aprismate`）。这些接口是：

- **累加式** — 不删除或重命名标准 OpenJDK API
- **版本化** — 每个能力跟踪引入和稳定性保证
- **可选** — 程序检测可用性并回退到标准 API
- **失效保护** — 查询失败返回 null/默认值，永不抛出异常

针对标准 OpenJDK 编译的程序在 AprismJDK 上运行不变，反之亦然（对于非 AprismJDK 子集）。

---

## 2. 模块结构

```
jdk.aprismate/
  jdk.aprismate.Agent           — AprismateAgent 程序化入口
  jdk.aprismate.VmInfo          — VM 身份和能力描述符
  jdk.aprismate.runtime/
    ThreadInsight               — 线程堆栈和调度内省
    HeapInsight                 — 堆区域和 GC 内省
    JitInsight                  — 编译队列和方法编译内省
```

---

## 3. 核心 API：`jdk.aprismate.VmInfo`

VM 构建身份和能力检测。

### 方法

```java
// 身份查询
public static String getAprismJdkVersion()
public static boolean isAprismJdk()
public static int getOpenJdkVersion()
public static String getVendor()

// 能力检测（使用前查询）
public static boolean hasClassRedefinerPlus()
public static boolean hasMethodHookRegistryPlus()
public static boolean hasBytecodeTransformer()
public static boolean hasVmIntrospection()
```

### 使用模式

```java
if (VmInfo.isAprismJdk()) {
    System.out.println("运行在 " + VmInfo.getAprismJdkVersion());
    
    if (VmInfo.hasClassRedefinerPlus()) {
        // 使用深度重定义能力
    } else {
        // 回退到标准 Instrumentation
    }
}
```

**标准 JDK 行为：** 所有 `has*` 方法返回 `false`；版本方法返回 `null`。

---

## 4. 核心 API：`jdk.aprismate.Agent`

AprismateAgent 能力的程序化入口点。

### 方法

```java
// 代理状态
public static boolean isAgentLoaded()
public static String getAgentVersion()

// 能力访问器（如果不可用返回 null）
public static Object getClassRedefiner()      // v26.1-Alpha.2
public static Object getMethodHookRegistry()  // v26.1-Alpha.4
public static Object getBytecodeTransformer() // v26.1-Alpha.5
```

### 使用模式

```java
if (Agent.isAgentLoaded()) {
    ClassRedefiner redefiner = (ClassRedefiner) Agent.getClassRedefiner();
    if (redefiner != null) {
        // 使用结构性重定义
    }
}
```

**标准 JDK 行为：** `isAgentLoaded()` 返回 `false`，除非通过标准 `-javaagent` 附加；能力访问器返回 `null`。

---

## 5. 运行时 API：`jdk.aprismate.runtime.ThreadInsight`

线程堆栈和调度内省（v26.1-Alpha.6）。

### 方法（计划中）

```java
// 线程状态查询
public static ThreadSnapshot[] getAllThreads()
public static ThreadSnapshot getThread(long threadId)
public static StackFrame[] captureStack(long threadId)

// 调度提示（仅建议）
public static long getThreadCpuTime(long threadId)
public static long getThreadUserTime(long threadId)
public static boolean isThreadBlocked(long threadId)
```

**标准 JDK 回退：** 使用 `ManagementFactory` 的 `ThreadMXBean`。

---

## 6. 运行时 API：`jdk.aprismate.runtime.HeapInsight`

堆区域和 GC 内省（v26.1-Alpha.6）。

### 方法（计划中）

```java
// 堆状态查询
public static HeapSummary getHeapSummary()
public static long getUsedMemory()
public static long getCommittedMemory()
public static long getMaxMemory()

// GC 内省
public static GcInfo[] getRecentCollections()
public static long getTotalCollectionTime()
public static long getCollectionCount()
```

**标准 JDK 回退：** 使用 `MemoryMXBean` 和 `GarbageCollectorMXBean`。

---

## 7. 运行时 API：`jdk.aprismate.runtime.JitInsight`

编译队列和方法编译内省（v26.1-Alpha.7）。

### 方法（计划中）

```java
// 编译状态
public static CompilationInfo[] getCompiledMethods()
public static boolean isMethodCompiled(Method method)
public static int getCompilationQueueSize()

// 编译提示
public static long getTotalCompilationTime()
public static void requestCompilation(Method method)  // 建议性
```

**标准 JDK 回退：** 使用 `CompilationMXBean`；可用信息有限。

---

## 8. 能力描述符格式

每个能力都有查询方法返回的描述符：

```java
public class CapabilityDescriptor {
    String name;              // 例如 "ClassRedefinerPlus"
    String version;           // 例如 "v26.1-Alpha.2"
    boolean available;        // 如果能力激活则为 true
    String fallbackStrategy;  // 描述标准 JDK 等效方案
}
```

这允许运行时内省可用内容。

---

## 9. 前向兼容契约

1. **查询能力，永不假设。** 为 AprismJDK 25 编写的模组必须在 AprismJDK 26+ 上运行，能力可能减少但仍可工作。

2. **优雅降级。** 每个 AprismJDK 特定 API 都有记录的标准 JDK 等效方案或无操作行为。

3. **版本化稳定性。** 一旦方法标记为稳定（GA 版本），它遵循语义版本控制：仅兼容性变化，带通知的弃用。

---

## 10. 实现状态

| API | v26.0-Alpha.1 | v26.0-Alpha.6 | v26.1-Alpha.6 | v26.1 GA |
|-----|---------------|---------------|---------------|----------|
| VmInfo（桩） | ✓ | ✓ | ✓ | ✓ |
| Agent（桩） | ✓ | ✓ | ✓ | ✓ |
| VmInfo（能力） | — | ✓ | ✓ | ✓ |
| Agent（能力） | — | — | ✓ | ✓ |
| ThreadInsight | — | — | ✓ | ✓ |
| HeapInsight | — | — | ✓ | ✓ |
| JitInsight | — | — | ✓ | ✓ |

---

## 11. 模块声明

`jdk.aprismate` 模块是 JDK 镜像的一部分：

```java
module jdk.aprismate {
    exports jdk.aprismate;
    exports jdk.aprismate.runtime;
    
    requires java.base;
    requires java.management;
    requires java.instrument;
}
```

**标准 JDK：** 模块不存在；使用它的代码必须通过 `ClassNotFoundException` 的 try-catch 检测缺失。

---

## 12. 测试策略

API 测试重点：

1. **存在测试** — API 在 AprismJDK 上可访问
2. **缺失测试** — 在标准 JDK 上的优雅行为
3. **契约测试** — 失效保护行为（null 返回，不抛出异常）
4. **回退测试** — 标准 JDK 等效方案产生类似结果

测试计数：~20（v26.0-Alpha.6）→ ~80（v26.1-Alpha.7）

---

## 13. 安全考虑

VM 内省 API 尊重 Java 安全模型：

- **SecurityManager 检查**（如果启用）— 如果权限不足，以 `SecurityException` 失败
- **模块边界** — 尊重 `--add-exports`/`--add-opens` 要求
- **无权限提升** — API 不绕过现有访问控制

---

## 14. 参考

- `java.lang.management` — 标准 JDK 管理 bean（回退 API）
- `java.lang.instrument` — 标准 instrumentation API
- JEP 403 — 强封装 JDK 内部（稳定表面的动机）
