package jdk.aprismate.invoke;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * GA-grade tests for FastReflection: correctness under concurrency,
 * cache behavior, and measured performance vs reflection baselines.
 */
class FastReflectionGaTest {

    static class Widget {
        private long id = 42L;
        int count;
        String label = "init";
        public static String sharedState = "initial";

        public String greet(String who) { return "hi " + who; }
        public int add(int a, int b) { return a + b; }
        public void fail() { throw new RuntimeException("boom"); }
        private String secret() { return "s3cr3t"; }
        public static long combine(long a, long b) { return a * 1000 + b; }
    }

    // ---------- concurrency ----------

    @Test
    void concurrentInvokersAreThreadSafe() throws Exception {
        Method greet = Widget.class.getMethod("greet", String.class);
        DirectInvoker inv = FastReflection.invoker(greet);
        Widget w = new Widget();

        int threads = 16;
        int iters = 5000;
        var latch = new CountDownLatch(threads);
        var errors = new AtomicInteger(0);
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        for (int t = 0; t < threads; t++) {
            final int tid = t;
            pool.submit(() -> {
                try {
                    latch.countDown();
                    latch.await();
                    for (int i = 0; i < iters; i++) {
                        Object r;
                        try {
                            r = inv.invoke(w, new Object[]{"t" + tid});
                        } catch (Throwable e) { errors.incrementAndGet(); return; }
                        if (!r.equals("hi t" + tid)) {
                            errors.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    errors.incrementAndGet();
                }
            });
        }
        pool.shutdown();
        assertThat(pool.awaitTermination(30, TimeUnit.SECONDS)).isTrue();
        assertThat(errors.get()).isZero();
    }

    @Test
    void concurrentFieldAccessThreadSafe() throws Exception {
        Field shared = Widget.class.getField("sharedState");
        DirectFieldAccessor acc = FastReflection.fieldAccessor(shared);

        int threads = 8;
        var latch = new CountDownLatch(threads);
        var pool = Executors.newFixedThreadPool(threads);
        var errors = new AtomicInteger(0);

        for (int t = 0; t < threads; t++) {
            final int tid = t;
            pool.submit(() -> {
                try {
                    latch.countDown();
                    latch.await();
                    for (int i = 0; i < 2000; i++) {
                        acc.set(null, "t" + tid + "-" + i);
                        Object v = acc.get(null);
                        if (v == null) errors.incrementAndGet();
                    }
                } catch (Throwable e) { errors.incrementAndGet(); }
            });
        }
        pool.shutdown();
        assertThat(pool.awaitTermination(30, TimeUnit.SECONDS)).isTrue();
        assertThat(errors.get()).isZero();
    }

    // ---------- cache behavior ----------

    @Test
    void repeatedAcquisitionReturnsConsistentInstance() throws Exception {
        Method m = Widget.class.getMethod("add", int.class, int.class);
        // FastReflection caches internally; repeated calls should be cheap
        var inv1 = FastReflection.invoker(m);
        var inv2 = FastReflection.invoker(m);
        assertThat(inv1).isSameAs(inv2); // if cached
    }

    // ---------- error propagation ----------

    @Test
    void exceptionUnwrappedAcrossTiers() throws Throwable {
        Method fail = Widget.class.getMethod("fail");
        DirectInvoker inv = FastReflection.invoker(fail);
        assertThatThrownBy(() -> inv.invoke(new Widget(), new Object[0]))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("boom");
    }

    @Test
    void privateMethodAccessibleViaAnyTier() throws Throwable {
        Method secret = Widget.class.getDeclaredMethod("secret");
        DirectInvoker inv = FastReflection.invoker(secret);
        assertThat(inv.invoke(new Widget(), new Object[0])).isEqualTo("s3cr3t");
    }

    // ---------- benchmark (informational, not CI-gated) ----------

    @Test
    void performanceBenchmark() throws Throwable {
        Method greet = Widget.class.getMethod("greet", String.class);
        Method add = Widget.class.getMethod("add", int.class, int.class);
        Field label = Widget.class.getDeclaredField("label");
        label.setAccessible(true);
        Widget w = new Widget();

        DirectInvoker greetInv = FastReflection.invoker(greet);
        DirectInvoker addInv = FastReflection.invoker(add);
        DirectFieldAccessor labelAcc = FastReflection.fieldAccessor(label);

        int ops = 1_000_000;
        Runnable directGreet = () -> w.greet("bench");
        Runnable fastGreet = () -> { try { greetInv.invoke(w, new Object[]{"bench"}); } catch (Throwable ignored) {} };
        Runnable reflGreet = () -> { try { greet.invoke(w, "bench"); } catch (Exception ignored) {} };
        Runnable directAdd = () -> w.add(2, 3);
        Runnable fastAdd = () -> { try { addInv.invoke(w, new Object[]{2, 3}); } catch (Throwable ignored) {} };
        Runnable reflAdd = () -> { try { add.invoke(w, 2, 3); } catch (Exception ignored) {} };
        Runnable directField = () -> { var x = w.label; };
        Runnable fastField = () -> { try { labelAcc.get(w); } catch (Throwable ignored) {} };
        Runnable reflField = () -> { try { label.get(w); } catch (Exception ignored) {} };

        var results = MicroBench.compare(5, 7, ops,
            "direct call", directGreet,
            "FastReflection MH", fastGreet,
            "Method.invoke", reflGreet,
            "direct int add", directAdd,
            "FastReflection add", fastAdd,
            "Method.invoke add", reflAdd,
            "direct field get", directField,
            "FastReflection field", fastField,
            "Field.get", reflField
        );

        System.out.println("=== Reflection Elimination Benchmark ===");
        System.out.println("(ops=" + ops + ", 5 warmup, 7 measured rounds, median)");
        results.values().forEach(r -> System.out.println("  " + r));

        // Sanity: FastReflection must not be catastrophically slower than
        // direct calls (>100x would indicate a bug, not a JIT limitation)
        double directNs = results.get("direct call").nsPerOp();
        double fastNs = results.get("FastReflection MH").nsPerOp();
        assertThat(fastNs / directNs).as("FastReflection/direct ratio").isLessThan(100.0);

        double reflNs = results.get("Method.invoke").nsPerOp();
        System.out.printf("  Method.invoke vs FastReflection: %.2fx%n",
                reflNs / Math.max(fastNs, 0.001));
    }
}
