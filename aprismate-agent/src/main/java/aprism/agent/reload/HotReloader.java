package aprism.agent.reload;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Applies a validated ChangeSet via retransformClasses. Atomic: either
 * all classes in the set are updated, or none are (JVM guarantees).
 *
 * <p>Fail-safe: errors are returned as results, never thrown.
 */
public final class HotReloader {

    private static volatile Instrumentation instrumentation;

    private HotReloader() {
    }

    public static void init(Instrumentation inst) {
        instrumentation = inst;
    }

    /**
     * Validates and applies a change set.
     *
     * @return list of per-class validation results, plus an overall
     *         "applied" flag in the last entry
     */
    public static ReloadResult apply(ChangeSet cs) {
        var inst = instrumentation;
        if (inst == null || !inst.isRetransformClassesSupported()) {
            return ReloadResult.failure("retransformation not available");
        }

        // Phase 1: validate each class
        var validations = new ArrayList<ChangeSetValidator.ValidationResult>();
        var originals = new LinkedHashMap<String, byte[]>();
        boolean allValid = true;

        for (var className : cs.classNames()) {
            try {
                Class<?> cls = Class.forName(className);
                byte[] current = captureBytes(cls);
                if (current == null) {
                    validations.add(new ChangeSetValidator.ValidationResult(
                            className, false, List.of("could not capture current bytecode")));
                    allValid = false;
                    continue;
                }
                originals.put(className, current);
                var vr = ChangeSetValidator.validate(className, current, cs.replacementFor(className));
                validations.add(vr);
                if (!vr.ok()) {
                    allValid = false;
                }
            } catch (ClassNotFoundException e) {
                validations.add(new ChangeSetValidator.ValidationResult(
                        className, false, List.of("class not found: " + className)));
                allValid = false;
            } catch (Throwable t) {
                validations.add(new ChangeSetValidator.ValidationResult(
                        className, false, List.of("validation error: " + t.getMessage())));
                allValid = false;
            }
        }

        if (!allValid) {
            return ReloadResult.validationFailed(validations);
        }

        // Phase 2: apply atomically via retransformClasses
        try {
            var classes = new ArrayList<Class<?>>();
            for (var className : cs.classNames()) {
                classes.add(Class.forName(className));
            }
            var oneShot = new BatchTransformer(cs);
            inst.addTransformer(oneShot, true);
            try {
                inst.retransformClasses(classes.toArray(Class<?>[]::new));
            } finally {
                inst.removeTransformer(oneShot);
            }
            return ReloadResult.success(validations);
        } catch (Throwable t) {
            return ReloadResult.failure("retransformation failed: "
                    + t.getClass().getSimpleName() + ": " + t.getMessage());
        }
    }

    private static byte[] captureBytes(Class<?> clazz) {
        String resource = clazz.getName().replace('.', '/') + ".class";
        try (var is = clazz.getClassLoader().getResourceAsStream(resource)) {
            return is != null ? is.readAllBytes() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private record BatchTransformer(ChangeSet cs) implements java.lang.instrument.ClassFileTransformer {
        @Override
        public byte[] transform(ClassLoader loader, String internalName,
                                Class<?> beingDefined, java.security.ProtectionDomain pd,
                                byte[] input) {
            String dotted = internalName.replace('/', '.');
            return cs.replacementFor(dotted);
        }
    }
}
