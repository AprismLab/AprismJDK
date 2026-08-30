package aprism.agent.reload;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Validates that a proposed replacement is safe to hot-swap.
 *
 * <p><b>Safe evolution rules</b>:
 * <ol>
 *   <li>Superclass + interfaces must be unchanged</li>
 *   <li>Class access flags must not lose public/protected</li>
 *   <li>Public/protected methods must not be removed</li>
 *   <li>Fields must not be removed (JVM retransformation constraint)</li>
 *   <li>Method bodies may change freely</li>
 * </ol>
 */
public final class ChangeSetValidator {

    public record ValidationResult(String className, boolean ok, List<String> violations) {
        static ValidationResult pass(String cls) { return new ValidationResult(cls, true, List.of()); }
        static ValidationResult fail(String cls, List<String> v) { return new ValidationResult(cls, false, List.copyOf(v)); }
    }

    private ChangeSetValidator() {
    }

    public static ValidationResult validate(String className, byte[] original, byte[] replacement) {
        var violations = new ArrayList<String>();
        var orig = readStructure(original);
        var repl = readStructure(replacement);

        if (!orig.superClass.equals(repl.superClass)) {
            violations.add("superclass changed: " + orig.superClass + " -> " + repl.superClass);
        }
        if (!orig.interfaces.equals(repl.interfaces)) {
            violations.add("interfaces changed");
        }
        if ((orig.access & Opcodes.ACC_PUBLIC) != (repl.access & Opcodes.ACC_PUBLIC)) {
            violations.add("class visibility changed");
        }
        for (var entry : orig.methods.entrySet()) {
            if (!repl.methods.containsKey(entry.getKey())) {
                int flags = entry.getValue();
                if ((flags & Opcodes.ACC_PUBLIC) != 0 || (flags & Opcodes.ACC_PROTECTED) != 0) {
                    violations.add("method removed: " + entry.getKey());
                }
            }
        }
        for (var entry : orig.fields.entrySet()) {
            if (!repl.fields.containsKey(entry.getKey())) {
                violations.add("field removed: " + entry.getKey());
            }
        }

        return violations.isEmpty()
                ? ValidationResult.pass(className)
                : ValidationResult.fail(className, violations);
    }

    static ClassStructure readStructure(byte[] bytes) {
        var reader = new ClassReader(bytes);
        var structure = new ClassStructure();
        reader.accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public void visit(int version, int access, String name, String sig,
                              String superClass, String[] interfaces) {
                structure.access = access;
                structure.superClass = superClass != null ? superClass.replace('/', '.') : "";
                structure.interfaces = Set.of(interfaces);
            }

            @Override
            public org.objectweb.asm.MethodVisitor visitMethod(int access, String mname,
                    String mdesc, String msig, String[] mexc) {
                structure.methods.put(mname + mdesc, access);
                return null;
            }

            @Override
            public org.objectweb.asm.FieldVisitor visitField(int faccess, String fname,
                    String fdesc, String fsig, Object fvalue) {
                structure.fields.put(fname + " " + fdesc, faccess);
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);
        return structure;
    }

    static final class ClassStructure {
        int access;
        String superClass = "";
        Set<String> interfaces = Set.of();
        Map<String, Integer> methods = new LinkedHashMap<>();
        Map<String, Integer> fields = new LinkedHashMap<>();
    }
}
