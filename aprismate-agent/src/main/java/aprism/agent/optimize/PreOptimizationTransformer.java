package aprism.agent.optimize;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Arrays;

/**
 * Opt-in ClassFileTransformer wiring the optimizer into class loading.
 *
 * <p>Fail-open contract: any error (cache IO, ASM parse, OOM) results in
 * {@code null} — the JVM then uses the ORIGINAL bytes. JDK-internal and
 * agent-owned packages are never touched.
 *
 * <p>Enabled explicitly via system property
 * {@code -Daprismate.optimizer.rules=<rules-file>} read at premain;
 * absent property means the transformer is never installed.
 */
public final class PreOptimizationTransformer implements ClassFileTransformer {

    private final OptimizerConfig config;
    private final AsmClassOptimizer optimizer;
    private final BytecodeCache cache;

    public PreOptimizationTransformer(OptimizerConfig config) {
        this.config = config;
        this.optimizer = new AsmClassOptimizer(config);
        this.cache = new BytecodeCache(config.cacheDir());
    }

    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> beingDefined, ProtectionDomain pd,
                            byte[] classfileBuffer) {
        if (className == null || isExcluded(className)) {
            return null;
        }
        try {
            String key = BytecodeCache.cacheKey(className, config.fingerprint(), classfileBuffer);
            byte[] cached = cache.get(key);
            if (cached != null) {
                return cached;
            }
            byte[] out = optimizer.transform(className, classfileBuffer);
            if (out == null) {
                return null;
            }
            cache.put(key, out);
            return out;
        } catch (Throwable t) {
            // fail-open: original bytes win
            return null;
        }
    }

    static boolean isExcluded(String internalName) {
        return internalName.startsWith("java/")
                || internalName.startsWith("javax/")
                || internalName.startsWith("jdk/")
                || internalName.startsWith("sun/")
                || internalName.startsWith("com/sun/")
                || internalName.startsWith("org/objectweb/asm/")
                || internalName.startsWith("aprism/agent/optimize/");
    }
}
