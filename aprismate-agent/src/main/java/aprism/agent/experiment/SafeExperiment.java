package aprism.agent.experiment;

import java.lang.instrument.Instrumentation;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Safe experimentation: propose a bytecode change, verify it loads, and
 * roll back if needed — all without crashing the host.
 *
 * <p>Uses {@link Instrumentation#retransformClasses} with a one-shot
 * transformer. If the replacement bytes are invalid (bad bytecode,
 * missing references), the JVM rejects the retransformation natively
 * and the class remains untouched. The original bytes are captured
 * before the attempt so rollback is always available.
 *
 * <p>Fail-safe: every public method catches Throwable; failures are
 * reported as results, never thrown.
 */
public final class SafeExperiment {

    private static final Map<String, ExperimentHandle> ACTIVE = new ConcurrentHashMap<>();
    private static volatile Instrumentation instrumentation;

    private SafeExperiment() {
    }

    public static void init(Instrumentation inst) {
        instrumentation = inst;
    }

    public static ExperimentResult tryReplace(Class<?> target, byte[] newBytes) {
        String name = target.getName();
        try {
            var inst = instrumentation;
            if (inst == null || !inst.isRetransformClassesSupported()) {
                return ExperimentResult.fail(name, "Unsupported",
                        "retransformation not available (is the agent attached?)");
            }

            byte[] current = captureBytes(target);
            if (current == null) {
                return ExperimentResult.fail(name, "CaptureFailed",
                        "could not read current bytecode for " + name);
            }
            final byte[] originalSnapshot = current.clone();

            var handle = new ExperimentHandle(name, Instant.now(), originalSnapshot);
            var oneShot = new OneShotTransformer(target.getName(), newBytes);

            inst.addTransformer(oneShot, true);
            try {
                inst.retransformClasses(target);
            } finally {
                inst.removeTransformer(oneShot);
            }

            ACTIVE.put(name, handle);
            return ExperimentResult.ok(handle);
        } catch (Throwable t) {
            String type = t.getClass().getSimpleName();
            return ExperimentResult.fail(name, type,
                    t.getMessage() != null ? t.getMessage() : type);
        }
    }

    public static boolean rollback(String className) {
        var handle = ACTIVE.remove(className);
        if (handle == null) {
            return false;
        }
        try {
            var inst = instrumentation;
            if (inst == null || !inst.isRetransformClassesSupported()) {
                return false;
            }
            var oneShot = new OneShotTransformer(className, handle.originalBytes());
            inst.addTransformer(oneShot, true);
            try {
                Class<?> cls = Class.forName(className);
                inst.retransformClasses(cls);
            } finally {
                inst.removeTransformer(oneShot);
            }
            return true;
        } catch (Throwable t) {
            ACTIVE.put(className, handle);
            return false;
        }
    }

    public static List<ExperimentHandle> activeExperiments() {
        return List.copyOf(ACTIVE.values());
    }

    private static byte[] captureBytes(Class<?> clazz) {
        String resource = clazz.getName().replace('.', '/') + ".class";
        try (var is = clazz.getClassLoader().getResourceAsStream(resource)) {
            if (is == null) {
                return null;
            }
            return is.readAllBytes();
        } catch (Exception e) {
            return null;
        }
    }

    private record OneShotTransformer(String targetInternalName, byte[] replacement)
            implements java.lang.instrument.ClassFileTransformer {

        @Override
        public byte[] transform(ClassLoader loader, String internalName,
                                Class<?> beingDefined, java.security.ProtectionDomain pd,
                                byte[] input) {
            if (internalName.equals(targetInternalName.replace('.', '/'))) {
                return replacement;
            }
            return null;
        }
    }
}
