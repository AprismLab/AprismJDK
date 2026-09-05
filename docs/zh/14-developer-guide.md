# AprismJDK 开发者指南

> v26.4 线。中文镜像；英文正本见 docs/14-developer-guide.md。

## 快速开始

### 使用完整 fork 镜像

```bash
unzip AprismJDK-26.3-windows-x64-jdk.zip
export JAVA_HOME=AprismJDK-26.3-windows-x64-jdk
$JAVA_HOME/bin/java -version

# 自动加载 agent
$JAVA_HOME/bin/java -XX:+AprismateAgent -jar myapp.jar

# 带诊断服务器
$JAVA_HOME/bin/java -XX:+AprismateAgent -Daprismate.diag.port=25590 -jar myapp.jar
curl http://127.0.0.1:25590/aprism/state
```

### 在 stock JDK 上使用 agent

```bash
java -javaagent:aprismate-26.3.jar -jar myapp.jar

# 带字节码预优化
java -javaagent:aprismate-26.3.jar \
     -Daprismate.optimizer.rules=rules.properties \
     -jar myapp.jar
```

### 从源码构建

```bash
# 前置：JDK 25（boot JDK）、MSYS2（Windows）或 build-essential（Linux）
git clone https://github.com/AprismLab/AprismJDK.git
cd AprismJDK

# JVM 模块构建 + 测试
./gradlew build

# OpenJDK fork 构建（Windows: scripts/configure-fork.sh）
cd openjdk-25
bash ../scripts/configure-fork.sh
make images JOBS=6 LOG=info
```

## API 指南

### 反射消除（`jdk.aprismate.invoke`）

用缓存的 MethodHandle invoker 替换 `Method.invoke()` 热路径：

```java
import jdk.aprismate.invoke.FastReflection;

// 获取一次（已缓存，线程安全）
Method greet = MyService.class.getMethod("greet", String.class);
DirectInvoker inv = FastReflection.invoker(greet);

// 热路径调用（~1.3x 直接调用，vs 原始反射 10-30x）
Object result = inv.invoke(serviceInstance, new Object[]{"world"});

// 异常解包透传（无 InvocationTargetException）
```

### 运行时状态导出（`jdk.aprismate.export`）

机器可读的 JVM 状态，面向 AI 代理和监控：

```java
import jdk.aprismate.export.RuntimeExporter;

// 完整 JSON 快照
String state = RuntimeExporter.full();

// 紧凑摘要（<2KB）
String brief = RuntimeExporter.summary();

// V2：历史 + diff + 告警
import jdk.aprismate.export.RuntimeExporterV2;
var diff = RuntimeExporterV2.takeSnapshot();
RuntimeExporterV2.setAlertListener(alert ->
    System.err.println("ALERT: " + alert.type() + " " + alert.detail()));
```

### 安全实验（`aprism.agent.experiment`）

提议字节码变更并回滚——无需重启：

```java
import aprism.agent.experiment.SafeExperiment;

var result = SafeExperiment.tryReplace(
    com.example.Service.class, optimizedBytes);

if (result.isSuccess()) {
    // 测试变更；不满意则回滚
    SafeExperiment.rollback("com.example.Service");
}
// 失败时：JVM 拒绝变更，类不变
```

### 热重载（`aprism.agent.reload`）

原子性多类替换 + 安全演化校验：

```java
import aprism.agent.reload.ChangeSet;
import aprism.agent.reload.HotReloader;

var cs = new ChangeSet("fix-1", Map.of(
    "com.example.Service", newServiceBytes,
    "com.example.Helper", newHelperBytes
));
var result = HotReloader.apply(cs);
// 原子操作：所有类交换或不交换
```

### 字节码预优化（`aprism.agent.optimize`）

在类加载时消除 debug 日志：

```properties
# rules.properties
elide = com.example.Debug log trace
probe-enter = com.example.Hot compute
```

```bash
java -javaagent:aprismate.jar \
     -Daprismate.optimizer.rules=rules.properties \
     -jar myapp.jar
```

### GC 调优预设（`jdk.aprismate.tuning`）

```java
import jdk.aprismate.tuning.GcPresets;
String flags = GcPresets.forName("server").orElseThrow().asLaunchArgs();
```

预设：`server`（吞吐）、`desktop`（ZGC 延迟）、`container`（cgroup 感知）、`compact`（serverless）。

### 诊断 HTTP 服务器（`aprism.agent.diag`）

```bash
java -javaagent:aprismate.jar -Daprismate.diag.port=25590 -jar myapp.jar

curl http://127.0.0.1:25590/aprism/state      # 完整 JSON
curl http://127.0.0.1:25590/aprism/summary    # <2KB 摘要
curl http://127.0.0.1:25590/aprism/health     # 存活
```

### SBOM 生成（`jdk.aprismate.secure`）

```java
import jdk.aprismate.secure.SbomGenerator;
String sbom = SbomGenerator.generate(artifacts, "v26.4");
// CycloneDX 1.5 JSON + SHA-256 哈希
```

## Agent 配置参考

| 属性 | 效果 | 默认 |
|---|---|---|
| `-Daprismate.diag.port=<n>` | 启动 HTTP 诊断服务器 | 禁用 |
| `-Daprismate.optimizer.rules=<file>` | 启用字节码预优化器 | 禁用 |
| `-Daprismate.startup.profile` | 启用启动类加载剖析器 | 禁用 |
| `-Daprismate.gc.profile=<name>` | 输出 GC 调优建议 | 禁用 |

## 从 stock OpenJDK 迁移

1. 将 `JAVA_HOME` 替换为 AprismJDK 镜像——所有标准 API 不变
2. 添加 `-XX:+AprismateAgent` 启用深层能力
3. 用 `VmInfo.isAprismJdk()` 检测运行时并升级行为
4. 所有 AprismJDK 专属 API 在 stock JDK 上优雅降级

## 从 v26.2 迁移到 v26.3+

- `jdk.aprismate.invoke` 替代临时 MethodHandle 接线
- `RuntimeExporter` 替代手动 MXBean 查询
- `SafeExperiment` 替代临时 retransformClasses 调用
- GC 预设替代手动 flag 调优
- Agent jar 现为自包含（打包 api + ASM 类）
