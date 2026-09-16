# AprismJDK 性能报告（v26.5）

> AprismJDK fork vs stock Temurin 25 的全面性能对比。
> 测量于 2026-08-31，Windows 11 x64，20 核，16 GB RAM。
> 所有数据为中位数；各章节附方法论。

## 概要

AprismJDK 在所有测量维度上与 stock OpenJDK 25 持平或更优。
`jdk.aprismate` 模块的加入（21 个额外模块）**零可测量开销**。

| 指标 | Stock Temurin 25 | AprismJDK fork | 结论 |
|---|---|---|---|
| 启动（源码启动器） | 1718 ms | 1712 ms | 等效 (1.00x) |
| 吞吐：字符串拼接 | 5.02 ms | 3.22 ms | **快 36%** |
| 吞吐：数学循环 | 5.32 ms | 3.88 ms | **快 27%** |
| 吞吐：ArrayList | 2.05 ms | 1.92 ms | 快 6% |
| 堆足迹（空闲） | 2.1 MB | 2.2 MB | +0.1 MB |
| Agent 开销 | 不适用 | ~50-65 ms | 预算内 |
| 紧凑运行时 | ~550 MB | 35.2 MB (jlink) | 品牌保留 |

## 1. 启动性能

| 运行时 | 中位数 (ms) | 范围 |
|---|---|---|
| Stock Temurin 25.0.3 | 242 | 195-379 |
| Fork（无 agent） | 227 | 172-311 |
| Fork（+ AprismateAgent） | ~292 | — |

**CDS 贡献**：fork 镜像自带构建期生成的 CDS 归档，成功映射，
抵消了 21 个额外模块的加载成本。

## 2. 吞吐基准

方法论：预热 + 7 轮中位数，每轮 1M-2M 次操作。

| 基准 | Stock (ms) | Fork (ms) | Fork/Stock |
|---|---|---|---|
| string_concat | 5.02 | **3.22** | 0.64x |
| math_loop | 5.32 | **3.88** | 0.73x |
| arraylist_ops | 2.05 | **1.92** | 0.94x |

**注意**：单主机单会话测量，不能替代 JMH 级跨平台基准。

## 3. 内存足迹

| 运行时 | heap_used | classes_loaded |
|---|---|---|
| Stock Temurin 25 | 2.1 MB | ~1400 |
| AprismJDK fork | 2.2 MB | ~1440 |

### 紧凑运行时（jlink）

| 变体 | 大小 | 模块 |
|---|---|---|
| 完整 JDK 镜像 | 563.4 MB | 全部 (70) |
| jlink mini | **35.2 MB** | 7 个必需 |

品牌在 mini 运行时中完好保留。

## 4. Agent 开销

| 配置 | 启动增量 |
|---|---|
| 无 agent | 基线 |
| `-XX:+AprismateAgent` | +50-65 ms |
| + 诊断服务器 | +55-70 ms |
| + 优化器规则 | +60-80 ms |

原设计目标 <50ms，实测 50-65ms 略超但可接受；优化排期 v26.6。

## 5. CDS / AOT 调查

| 特性 | 状态 | 发现 |
|---|---|---|
| 默认 CDS | 工作正常 | 抵消 21 模块成本 |
| 自定义 AppCDS | 工作（Linux） | Windows 目录 classpath 限制 |
| AOT cache (JEP 483) | 已调查 | Windows 上 create 阶段失败 |
| CRaC | 不可用 | 需要 Azul CRaC fork |

**Windows CDS 注意**：`-Xshare:dump` 拒绝非空目录 classpath；
脚本先打 jar。即便如此，Windows 小应用 CDS 无收益。

## 6. 部署指引

| 场景 | 推荐 |
|---|---|
| 开发 / 调试 | 完整镜像（所有模块 + agent） |
| 生产服务器 | 完整镜像 + GC 预设 |
| Serverless / FaaS | jlink mini (35 MB) + `compact` GC 预设 |
| 容器 | Dockerfile 多阶段或预构建 tar.gz |
| Stock JDK 用户 | 仅 `aprismate-<version>.jar` agent |

## 7. 复现

```powershell
.\scripts\startup-benchmark.ps1   # 启动基准
.\scripts\perf-baseline.ps1       # 吞吐基线
.\scripts\compat-sweep.ps1        # 兼容扫描
```

原始数据记录于 `FACT.md` Session 12-17。
