# AprismateAgent 设计文档

> AprismJDK 子项目文档。描述 AprismateAgent 组件：入口点、能力和失效保护契约。
>
> 作者：BlockConnect@StarsailsClover。双语：EN（权威）+ ZH（镜像）。
> v26.0-Alpha.1 交付（设计），v26.1 线实现。

---

## 1. 概述

**AprismateAgent** 是 AprismJDK 的旗舰组件：一个类似 JavaAgent 的代理，*捆绑在* JDK 镜像中，无需任何外部 jar 即可访问。它为 Aprism 加载器生态系统提供深度 VM 集成，同时通过优雅降级保持与标准 OpenJDK 的兼容性。

代理围绕三个原则设计：

1. **失效保护** — 错误的钩子或转换会被记录和隔离，永远不会使宿主应用程序或 JVM 崩溃。
2. **可选** — 所有 Aprism 能力都能在标准 OpenJDK 上工作；AprismateAgent 只是解锁了更深层次的能力。
3. **版本化** — 能力通过版本化描述符公开；模组查询能力而不是假设它们。

---

## 2. 入口点

AprismateAgent 支持两种标准 JavaAgent 模式：

### 2.1 Premain（加载时附加）

```java
public static void premain(String agentArgs, Instrumentation inst)
```

当通过 `-javaagent` 标志指定代理时，在应用程序的 `main` 方法之前调用：

```bash
java -javaagent:aprismate-agent.jar=key1=value1;key2=value2 ...
```

**用例：**
- 加载时字节码织入
- 启动时钩子注册
- 早期 VM 状态捕获

### 2.2 Agentmain（运行时附加）

```java
public static void agentmain(String agentArgs, Instrumentation inst)
```

通过 Attach API 附加到正在运行的 JVM 时调用：

```java
VirtualMachine vm = VirtualMachine.attach(pid);
vm.loadAgent("/path/to/aprismate-agent.jar", "args");
vm.detach();
```

**用例：**
- 热附加钩子到运行中的应用程序
- 运行时诊断和性能分析
- 动态模组加载

### 2.3 自动加载模式（仅 AprismJDK）

在 AprismJDK 上，代理可以通过 JVM 标志自动加载：

```bash
java -XX:+AprismateAgent ...
```

这会在 `main` 之前连接代理，无需 `-javaagent`。VM 知道代理的存在，可以在标准代理无法到达的点安装钩子。

**状态：** 推迟到 v26.2+（需要 VM 级集成）

---

## 3. 代理参数

参数以分号分隔的 key=value 对传递：

```
key1=value1;key2=value2;flag
```

### 支持的参数（v26.0-Alpha.1）

尚无（骨架实现）。计划在 v26.0-Alpha.5：

- `debug=true` — 启用详细日志
- `transform=<pattern>` — 类转换过滤器
- `hooks=<file>` — 从文件加载钩子定义

---

## 4. 能力

代理通过 `jdk.aprismate.Agent` API 公开能力。

### 4.1 ClassRedefiner+（v26.1-Alpha.2）

重定义类，包括标准 `Instrumentation.redefineClasses` 拒绝的结构性变化（添加/删除字段/方法）。

**标准 JDK 回退：** 使用标准 `Instrumentation.redefineClasses`，有限制（无结构性变化）。

**失效保护：** 无效的重定义会被记录并拒绝；现有类定义保持不变。

### 4.2 MethodHookRegistry+（v26.1-Alpha.4）

在任何方法上注册进入/退出钩子，包括 JIT 编译的方法。VM 通过将钩子点视为去优化锚点来保证钩子在内联后仍然有效。

**标准 JDK 回退：** 使用 ASM 方法包装或 Java 代理转换（效率较低，无 JIT 保证）。

**失效保护：** 错误的钩子（异常、无限循环）会被捕获并禁用；被钩住的方法继续正常执行。

### 4.3 BytecodeTransformer（v26.1-Alpha.5）

基于 ASM 的管道钩子，在加载时、验证之前看到类，支持类似 Mixin 的织入，无需单独的 Mixin 运行时。

**标准 JDK 回退：** 使用标准 `ClassFileTransformer`（在所有 JVM 上工作）。

**失效保护：** 失败的转换回退到未转换的类；验证错误被记录。

### 4.4 VmIntrospection（v26.1-Alpha.6+）

通过命名方法而不是 JMX 反射读取线程堆栈、类统计、堆摘要、JIT/GC 状态。

**标准 JDK 回退：** 使用 `ManagementFactory` MXBeans（标准但基于反射）。

**失效保护：** 查询失败返回 null 或空结果；永不抛出异常。

---

## 5. 失效保护契约

每个代理能力都是**对应用程序失效关闭，而非对 VM**：

- **错误的钩子** — 记录并跳过；方法正常执行
- **错误的转换** — 记录；加载未转换的类
- **无效的重定义** — 记录；现有类保持不变
- **查询失败** — 返回 null/空；永不崩溃

**保证：** 代理绝不能使 JVM 崩溃。这反映了 Aprism 应用于其加载器的失效保护规则。

---

## 6. 清单属性

代理 jar 包含这些清单属性：

```
Premain-Class: com.aprismate.agent.AprismateAgent
Agent-Class: com.aprismate.agent.AprismateAgent
Can-Redefine-Classes: true
Can-Retransform-Classes: true
Can-Set-Native-Method-Prefix: true
Implementation-Title: AprismateAgent
Implementation-Version: v26.0-Alpha.1
Implementation-Vendor: AprismLab
```

---

## 7. 与 Aprism 加载器的集成

Aprism 加载器在运行时检测 AprismateAgent：

```java
if (jdk.aprismate.VmInfo.isAprismJdk()) {
    // 使用 AprismJDK 特定 API
    ClassRedefiner redefiner = (ClassRedefiner) Agent.getClassRedefiner();
} else {
    // 回退到标准 JDK 行为
    instrumentation.redefineClasses(...);
}
```

此检测发生在 Aprism 的深层 API 层（v26.4 线），对模组透明。

---

## 8. 实现状态

- **v26.0-Alpha.1**：骨架（premain/agentmain 入口点，基本初始化）
- **v26.0-Alpha.5**：参数解析，日志基础设施
- **v26.0-Alpha.7**：Instrumentation 集成，基本附加验证
- **v26.1-Alpha.2**：ClassRedefiner+ 实现
- **v26.1-Alpha.4**：MethodHookRegistry+ 实现
- **v26.1-Alpha.5**：BytecodeTransformer 实现
- **v26.1-Alpha.6+**：VmIntrospection 实现

---

## 9. 测试策略

代理测试需要特殊的测试基础设施：

1. **单元测试** — API 契约、参数解析
2. **集成测试** — 代理附加（premain/agentmain）
3. **转换测试** — 字节码织入验证
4. **失效保护测试** — 错误的钩子、无效转换（必须不崩溃）
5. **真实游戏测试** — 与 Minecraft 上的 Aprism 加载器集成

测试计数跟踪：0（v26.0-Alpha.1）→ ~30（v26.0-Alpha.7）→ ~150（v26.1-Alpha.5）

---

## 10. 参考

- `java.lang.instrument.Instrumentation` — 标准 Java 代理 API
- JVMTI — JVM 工具接口（本地代理基础）
- ASM 库 — 字节码操作框架
- Aprism ClassRedefiner/MethodHookRegistry — 加载器侧集成点
