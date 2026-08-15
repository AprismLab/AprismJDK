# Minecraft JE Optimization Integration Plan

**Target:** AprismJDK v26.1 line  
**Reference:** MCJEBooster limitations analysis  
**Created:** 2026-08-11  
**Status:** Planning

---

## Executive Summary

MCJEBooster 受限于 Java Agent 能力，无法实现深层优化。AprismJDK 作为完整的 JDK 分支，可以在 JVM 层面提供更强大的 Minecraft 优化能力。

### MCJEBooster 的局限性

| 限制类型 | MCJEBooster 现状 | AprismJDK 潜力 |
|---------|-----------------|---------------|
| **字节码操作** | ASM 运行时转换，overhead 大 | 启动时 AOT 优化，零运行时开销 |
| **内存管理** | 无法干预 GC | 定制 GC 策略（区域化 GC） |
| **线程调度** | 受限于 JVM 线程模型 | 自定义线程调度器 |
| **JIT 编译** | 无法影响 | 针对 MC 热点代码预编译 |
| **反射性能** | 只能缓存 MethodHandle | 直接生成调用字节码 |
| **全局同步** | 需要复杂的协调逻辑 | JVM 级别的轻量级同步原语 |

### 性能提升预期

| 优化项 | MCJEBooster 提升 | AprismJDK 目标提升 |
|-------|-----------------|-------------------|
| 反射调用 | 35% (MethodHandle) | 80-90% (直接字节码) |
| GC 停顿 | 无改善 | 60-70% (区域化 GC) |
| JIT 预热 | 无改善 | 消除冷启动延迟 |
| 线程上下文切换 | 部分改善 | 70% (绿色线程) |
| **总体 TPS** | 1.5-2.5x | 3.0-5.0x |

---

## Implementation Phases

### Phase 1: Foundation (v26.1-Alpha.1 ~ Alpha.3)

**Timeline:** 已完成部分基础，待增强  
**Status:** ✅ VmInfo, Event, Mod APIs 已就绪

#### 1.1 Minecraft Detection & Profiling API

**Module:** `jdk.aprismate.minecraft`

```java
package jdk.aprismate.minecraft;

public interface MinecraftDetector {
    /**
     * 检测当前 JVM 是否在运行 Minecraft
     * 分析：
     * - 检查主类名（net.minecraft.client.main.Main 等）
     * - 检查加载的类签名（MinecraftServer, Level 等）
     * - 检查 Mod 加载器（Forge, Fabric, NeoForge）
     */
    MinecraftRuntime detect();
    
    /**
     * 获取 Minecraft 版本信息
     * - 版本号（1.8.9 - 1.26.x）
     * - 侧面（Client / Server / Integrated）
     * - Mod 加载器类型和版本
     */
    MinecraftVersion getVersion();
    
    /**
     * 判断是否需要启用优化
     * - 排除开发环境（IDE 运行）
     * - 排除不兼容的 Mod（已知冲突）
     * - 检查用户配置（.aprismate/minecraft-opt.yml）
     */
    boolean shouldOptimize();
}

public interface MinecraftProfiler {
    /**
     * 实时性能剖析
     * - Tick 时长分布
     * - 实体密度热图
     * - 区块加载/卸载频率
     * - 红石电路活跃度
     */
    ProfileSnapshot captureSnapshot();
    
    /**
     * 热点检测
     * - 识别 TPS 瓶颈（实体 AI、方块更新、网络）
     * - 建议优化策略
     */
    List<Hotspot> detectHotspots();
}
```

**Implementation Target:** v26.1-Alpha.1 (当前 v26.0-Alpha.8 完成后)

**Deliverables:**
- `aprismate-minecraft/` 新模块
- `jdk.aprismate.minecraft` API
- 34 个版本适配器（复用 MCJEBooster 研究成果）
- 20+ 单元测试

---

#### 1.2 Bytecode Pre-Optimization Engine

**Problem:** MCJEBooster 运行时 ASM 转换有 overhead

**Solution:** AprismJDK 启动时一次性优化

```java
package jdk.aprismate.transform;

public interface BytecodePreOptimizer {
    /**
     * 在类加载前优化字节码
     * - 消除反射调用（生成直接调用字节码）
     * - 内联小方法（tick 热路径）
     * - 移除冗余边界检查
     */
    byte[] optimize(String className, byte[] originalBytecode);
    
    /**
     * 针对 Minecraft 特定模式优化
     * - MinecraftServer.tick() 循环展开
     * - Entity.tick() 向量化
     * - ChunkProvider.getChunk() 去虚拟化
     */
    byte[] optimizeForMinecraft(String className, byte[] bytecode);
}
```

**Implementation Target:** v26.1-Alpha.2

**Technical Approach:**
- 使用 ASM 9.7+ 的 ClassWriter.COMPUTE_FRAMES
- 在 ClassLoader.defineClass() 前拦截
- 缓存优化结果到 `~/.aprismate/bytecode-cache/`

**Benchmark Target:**
- 反射调用消除：80-90% 性能提升
- 方法内联：15-25% 性能提升

---

#### 1.3 Reflection Elimination Framework

**Problem:** MCJEBooster 跨版本兼容依赖反射，损失 50% 性能

**Solution:** 运行时生成适配器字节码

```java
package jdk.aprismate.reflection;

public interface ReflectionAdapter {
    /**
     * 为反射调用生成直接字节码
     * 
     * 示例：
     * 原代码：method.invoke(entity, arg)
     * 生成：entity.tick(arg)
     */
    <T> T generateDirectInvoker(Method method);
    
    /**
     * 字段访问优化
     * 
     * 原代码：field.get(object)
     * 生成：object.fieldName
     */
    <T> FieldAccessor<T> generateFieldAccessor(Field field);
}

// 生成的字节码示例（反编译后）
public class Entity_tick_Invoker implements MethodInvoker {
    public Object invoke(Object target, Object... args) {
        return ((Entity) target).tick((World) args[0]);
    }
}
```

**Implementation Target:** v26.1-Alpha.2

**Expected Performance:**
- 从 MethodHandle 的 1.3x overhead → 直接调用的 1.0x
- MCJEBooster 25ms/tick → AprismJDK 3ms/tick (5000 实体场景)

---

### Phase 2: Core Optimizations (v26.1-Alpha.4 ~ Alpha.6)

**Timeline:** v26.0-GA 发布后 2-3 个月  
**Prerequisite:** Phase 1 APIs 稳定

#### 2.1 Regionalized Garbage Collection

**Problem:** 全局 GC 停顿影响 Minecraft tick 稳定性

**Solution:** 区域化 GC，每个 Region 独立回收

```java
package jdk.aprismate.gc;

public interface RegionalGC {
    /**
     * 定义 GC 区域
     * - 每个 Minecraft 区块 Region 映射到独立 GC Region
     * - 玩家附近 Region 高频 GC（降低停顿）
     * - 远离玩家 Region 低频 GC（降低开销）
     */
    void defineRegion(String regionId, RegionProfile profile);
    
    /**
     * 触发增量 GC
     * - 在 tick 间隙执行（< 5ms）
     * - 避免全局 Stop-The-World
     */
    void incrementalCollect(String regionId);
}
```

**Implementation Target:** v26.1-Alpha.4

**Technical Approach:**
- 基于 ZGC / Shenandoah 的区域化改造
- 为 Minecraft Region 分配独立 TLAB (Thread-Local Allocation Buffer)
- 在 `MinecraftServer.tick()` 后触发增量 GC

**Expected Performance:**
- GC 停顿：60-70% 减少
- TPS 稳定性：90% 减少方差

---

#### 2.2 JIT Pre-Compilation for Hotspots

**Problem:** Minecraft 冷启动时 JIT 未预热，TPS 不稳定

**Solution:** 预编译已知热点代码

```java
package jdk.aprismate.jit;

public interface JitPreCompiler {
    /**
     * 预编译 Minecraft 热点方法
     * - MinecraftServer.tick()
     * - Entity.tick()
     * - ChunkProvider.getChunk()
     * - WorldRenderer.renderWorld()
     */
    void precompileHotspots();
    
    /**
     * 加载预编译配置
     * - 从 MCJEBooster 研究数据生成
     * - 包含 34 个 Minecraft 版本的热点列表
     */
    void loadProfile(String minecraftVersion);
}
```

**Implementation Target:** v26.1-Alpha.5

**Technical Approach:**
- 使用 JVMCI (JVM Compiler Interface)
- 在 JVM 启动时预编译（并行，不阻塞主线程）
- 缓存编译结果到 `~/.aprismate/jit-cache/<version>/`

**Expected Performance:**
- 消除前 5 分钟的 TPS 波动
- 稳定达到最优 TPS

---

#### 2.3 Lightweight Thread Scheduler

**Problem:** MCJEBooster 受限于 Java 线程模型，上下文切换开销大

**Solution:** 绿色线程（Fiber）用于 Region 调度

```java
package jdk.aprismate.thread;

public interface FiberScheduler {
    /**
     * 为每个 Minecraft Region 创建轻量级 Fiber
     * - 消除线程上下文切换开销（~10μs → ~100ns）
     * - 支持数千个并发 Region（传统线程受限于数百）
     */
    Fiber createFiber(Runnable task);
    
    /**
     * 协作式调度
     * - Region 完成 tick 后主动让出 CPU
     * - 避免抢占式调度的 cache miss
     */
    void yield();
}
```

**Implementation Target:** v26.1-Alpha.6

**Technical Approach:**
- 基于 Project Loom (Virtual Threads) 实现
- 每个 Minecraft Region 在独立 Fiber 中运行
- 主线程协调 Fiber 调度

**Expected Performance:**
- 上下文切换开销：70% 减少
- 支持更细粒度的 Region 划分

---

### Phase 3: Advanced Features (v26.1-Alpha.7 ~ GA)

**Timeline:** v26.1-Alpha.6 完成后 2-3 个月  
**Focus:** 超越 MCJEBooster / Folia 的创新功能

#### 3.1 Cross-Region Async Communication

**Problem:** Folia 完全移除全局同步，但跨 Region 交互延迟高

**Solution:** AprismJDK 提供高效的跨 Region 消息传递

```java
package jdk.aprismate.region;

public interface RegionMessaging {
    /**
     * 异步消息传递（无锁）
     * - 使用 LMAX Disruptor 无锁队列
     * - 单向延迟 < 50μs
     */
    void sendAsync(RegionId target, Message msg);
    
    /**
     * 批量合并（减少同步点）
     * - 收集一个 tick 内的所有跨 Region 操作
     * - 在 tick 边界统一处理
     */
    void flushBatch();
}
```

**Implementation Target:** v26.1-Alpha.7

---

#### 3.2 Predictive Load Balancing

**Problem:** MCJEBooster 静态 Region 导致负载不均（25:1 方差）

**Solution:** 机器学习预测负载，动态调整 Region

```java
package jdk.aprismate.balancer;

public interface PredictiveBalancer {
    /**
     * 基于历史数据预测负载
     * - 实体移动趋势
     * - 玩家行为模式
     * - 红石电路活跃周期
     */
    LoadPrediction predict(RegionId region, int ticksAhead);
    
    /**
     * 动态调整 Region 大小
     * - 高负载 Region 拆分
     * - 低负载 Region 合并
     */
    void rebalance();
}
```

**Implementation Target:** v26.1-Alpha.8

**Technical Approach:**
- 简单的线性回归模型（避免过度复杂）
- 训练数据来自 `MinecraftProfiler` 快照
- 每 100 tick 评估一次，每 500 tick 调整一次

---

#### 3.3 Client-Side Optimization

**Problem:** MCJEBooster 仅支持服务端，客户端性能未优化

**Solution:** 渲染线程 + 区块网格优化

```java
package jdk.aprismate.client;

public interface RenderOptimizer {
    /**
     * 区块网格批处理
     * - 合并相邻区块的渲染调用
     * - 减少 OpenGL draw call
     */
    void batchChunkMeshes();
    
    /**
     * 异步区块加载
     * - 在后台线程生成区块网格
     * - 主渲染线程无阻塞
     */
    CompletableFuture<ChunkMesh> loadAsync(ChunkPos pos);
}
```

**Implementation Target:** v26.1-Alpha.9

---

### Phase 4: Integration & Release (v26.1-GA)

**Timeline:** v26.1-Alpha.9 完成后 1 个月  
**Focus:** 稳定性、文档、生态整合

#### 4.1 MCJEBooster Migration Path

**Goal:** 让 MCJEBooster 用户无缝迁移到 AprismJDK

**Deliverables:**
1. **兼容层**
   - AprismJDK 自动检测 MCJEBooster Agent
   - 提示用户卸载 MCJEBooster（功能已内置）

2. **配置迁移工具**
   ```bash
   aprism-minecraft migrate-from-mcjebooster
   ```
   - 自动转换 `mcjebooster.yml` → `.aprismate/minecraft-opt.yml`

3. **性能对比报告**
   - 自动生成 MCJEBooster vs AprismJDK 性能对比
   - 可视化 TPS 提升、GC 停顿改善

#### 4.2 Launcher Integration

**Supported Launchers:**
- ✅ HMCL (Hello Minecraft Launcher)
- ✅ PCL2 (Plain Craft Launcher 2)
- ✅ Prism Launcher
- ✅ Official Launcher (通过 Java 路径替换)

**Integration Method:**
```yaml
# HMCL 配置示例
javaPath: "C:/AprismJDK/bin/javaw.exe"
jvmArgs:
  - "-XX:+UseAprismOptimizations"
  - "-XX:AprismMinecraftMode=auto"
```

#### 4.3 Documentation

**English:**
- `docs/en/13-minecraft-optimization-guide.md`
- `docs/en/14-migration-from-mcjebooster.md`

**Chinese:**
- `docs/zh/13-minecraft-optimization-guide.md`
- `docs/zh/14-migration-from-mcjebooster.md`

---

## Implementation Timeline

```
v26.0 Line (Current - 2 weeks)
├── v26.0-Alpha.8 ✅ Compatibility (DONE)
└── v26.0-Alpha.9 ⏳ Final API polish
└── v26.0-GA 🎯 Official Release (2 weeks)

v26.1 Line (2-6 months)
├── Phase 1: Foundation (2-3 weeks)
│   ├── v26.1-Alpha.1: MinecraftDetector + Profiler API
│   ├── v26.1-Alpha.2: BytecodePreOptimizer + ReflectionAdapter
│   └── v26.1-Alpha.3: 34 version adapters (from MCJEBooster)
│
├── Phase 2: Core Optimizations (2-3 months)
│   ├── v26.1-Alpha.4: RegionalGC
│   ├── v26.1-Alpha.5: JIT Pre-Compilation
│   └── v26.1-Alpha.6: Fiber Scheduler
│
├── Phase 3: Advanced Features (2-3 months)
│   ├── v26.1-Alpha.7: Cross-Region Messaging
│   ├── v26.1-Alpha.8: Predictive Load Balancing
│   └── v26.1-Alpha.9: Client-Side Optimization
│
└── Phase 4: Release (1 month)
    └── v26.1-GA: MCJEBooster migration + Documentation
```

---

## Decision Points

### When to Archive MCJEBooster?

**Criteria:**
1. ✅ AprismJDK v26.1-Alpha.6 完成核心优化
2. ✅ 性能测试证明 AprismJDK > MCJEBooster (3x vs 2.5x)
3. ✅ 至少 100 用户成功迁移
4. ✅ 文档完善（EN + ZH）

**Archive Action:**
```markdown
# MCJEBooster (Archived)

This project has been superseded by [AprismJDK](https://github.com/NDBlockConnect/AprismJDK).

AprismJDK provides all MCJEBooster features plus:
- 3-5x TPS improvement (vs 2.5x)
- 70% lower GC pauses
- Client-side optimization
- Zero runtime overhead

See [Migration Guide](https://aprismjdk.dev/docs/migrate-from-mcjebooster).
```

### Integration Strategy

**Option 1: Separate Module (Recommended)**
- `aprismate-minecraft/` 作为独立模块
- 不影响 AprismJDK 核心稳定性
- 可独立迭代

**Option 2: Built-in JVM**
- 集成到 HotSpot 源码
- 更深层优化
- 但维护成本高

**Decision:** 采用 Option 1，v26.1 line 作为独立模块，v27.0 line 考虑集成到 JVM

---

## Testing Strategy

### Unit Tests
- 每个 Alpha 版本新增 20-30 测试
- 目标：v26.1-GA 时达到 300+ 测试

### Integration Tests
- 自动化 Minecraft 服务器启动测试
- 模拟 1000 实体、10 玩家场景
- 验证 TPS > 20

### Performance Regression Tests
- 每次 commit 运行基准测试
- TPS 下降 > 5% 触发 CI 失败

### Compatibility Tests
- 34 个 Minecraft 版本自动化测试
- 覆盖 Forge / Fabric / NeoForge

---

## Risk Analysis

| 风险 | 影响 | 概率 | 缓解措施 |
|------|------|------|---------|
| Minecraft 更新破坏兼容性 | 高 | 中 | 版本检测 + 自动降级 |
| GC 改造引入内存泄漏 | 高 | 低 | 充分测试 + 金丝雀发布 |
| JIT 预编译失败 | 中 | 中 | 回退到标准 JIT |
| Mod 冲突 | 中 | 高 | 黑名单机制 |

---

## Success Metrics

### Performance
- ✅ TPS 提升 3-5x (vs vanilla)
- ✅ GC 停顿 < 10ms (99th percentile)
- ✅ 冷启动 TPS 稳定性 > 95%

### Adoption
- 🎯 v26.1-GA 发布后 6 个月内 1000+ 活跃用户
- 🎯 至少 3 个主流 Launcher 官方集成

### Ecosystem
- 🎯 归档 MCJEBooster，统一到 AprismJDK
- 🎯 社区贡献 > 10 PR

---

## References

1. **MCJEBooster Research**
   - `RESEARCH_SUMMARY.md` - Folia 实测数据
   - `PERFORMANCE_ANALYSIS.md` - 性能瓶颈分析

2. **Academic Papers**
   - "Scalable Parallel Discrete Event Simulation" (2019)
   - "Region-based Memory Management" (2021)

3. **Industry Best Practices**
   - Paper MC (Folia) 架构文档
   - ZGC / Shenandoah GC 设计文档

---

**Next Steps:**
1. 完成 v26.0-Alpha.9 + GA
2. 创建 `aprismate-minecraft/` 模块骨架
3. 开始 v26.1-Alpha.1 实现
