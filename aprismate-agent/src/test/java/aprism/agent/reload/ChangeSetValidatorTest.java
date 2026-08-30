package aprism.agent.reload;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChangeSetValidatorTest {

    // ---------- bytecode fixtures ----------

    private static byte[] makeClass(String name, String superClass,
                                     String[] interfaces, String[][] methods) {
        var cw = new ClassWriter(0);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, name.replace('.', '/'),
                null, superClass != null ? superClass.replace('.', '/') : "java/lang/Object",
                interfaces);
        for (var m : methods) {
            var mv = cw.visitMethod(Opcodes.ACC_PUBLIC, m[0], m[1], null, null);
            mv.visitCode();
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        cw.visitEnd();
        return cw.toByteArray();
    }

    // ---------- validation rules ----------

    @Test
    void identicalClassesPass() {
        byte[] orig = makeClass("test.A", null, null,
                new String[][]{{"run", "()V"}, {"get", "()I"}});
        byte[] repl = makeClass("test.A", null, null,
                new String[][]{{"run", "()V"}, {"get", "()I"}});

        var vr = ChangeSetValidator.validate("test.A", orig, repl);
        assertThat(vr.ok()).isTrue();
    }

    @Test
    void methodBodyChangePasses() {
        // Same signatures, different body (just more NOPs) — should pass
        byte[] orig = makeClass("test.B", null, null, new String[][]{{"run", "()V"}});
        var cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "test/B", null, "java/lang/Object", null);
        var mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "run", "()V", null, null);
        mv.visitCode();
        for (int i = 0; i < 10; i++) mv.visitInsn(Opcodes.NOP);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();
        byte[] repl = cw.toByteArray();

        assertThat(ChangeSetValidator.validate("test.B", orig, repl).ok()).isTrue();
    }

    @Test
    void superclassChangeFails() {
        byte[] orig = makeClass("test.C", "java/lang/Object", null, new String[][]{{"x", "()V"}});
        byte[] repl = makeClass("test.C", "java/lang/Thread", null, new String[][]{{"x", "()V"}});

        var vr = ChangeSetValidator.validate("test.C", orig, repl);
        assertThat(vr.ok()).isFalse();
        assertThat(vr.violations()).anyMatch(v -> v.contains("superclass"));
    }

    @Test
    void publicMethodRemovalFails() {
        byte[] orig = makeClass("test.D", null, null,
                new String[][]{{"keep", "()V"}, {"drop", "()V"}});
        byte[] repl = makeClass("test.D", null, null, new String[][]{{"keep", "()V"}});

        var vr = ChangeSetValidator.validate("test.D", orig, repl);
        assertThat(vr.ok()).isFalse();
        assertThat(vr.violations()).anyMatch(v -> v.contains("drop"));
    }

    @Test
    void interfaceChangeFails() {
        byte[] orig = makeClass("test.E", null, new String[]{"java/lang/Runnable"},
                new String[][]{{"run", "()V"}});
        byte[] repl = makeClass("test.E", null, null, new String[][]{{"run", "()V"}});

        var vr = ChangeSetValidator.validate("test.E", orig, repl);
        assertThat(vr.ok()).isFalse();
        assertThat(vr.violations()).anyMatch(v -> v.contains("interface"));
    }

    @Test
    void addingMethodIsSafe() {
        byte[] orig = makeClass("test.F", null, null, new String[][]{{"old", "()V"}});
        byte[] repl = makeClass("test.F", null, null,
                new String[][]{{"old", "()V"}, {"new", "()I"}});

        var vr = ChangeSetValidator.validate("test.F", orig, repl);
        assertThat(vr.ok()).isTrue();
    }

    @Test
    void fieldRemovalFails() {
        // Build class with a field
        var cw1 = new ClassWriter(0);
        cw1.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "test/G", null, "java/lang/Object", null);
        cw1.visitField(Opcodes.ACC_PUBLIC, "count", "I", null, null).visitEnd();
        var mv1 = cw1.visitMethod(Opcodes.ACC_PUBLIC, "run", "()V", null, null);
        mv1.visitCode(); mv1.visitInsn(Opcodes.RETURN); mv1.visitMaxs(0,0); mv1.visitEnd();
        cw1.visitEnd();
        byte[] orig = cw1.toByteArray();

        // Replacement without the field
        var cw2 = new ClassWriter(0);
        cw2.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "test/G", null, "java/lang/Object", null);
        var mv2 = cw2.visitMethod(Opcodes.ACC_PUBLIC, "run", "()V", null, null);
        mv2.visitCode(); mv2.visitInsn(Opcodes.RETURN); mv2.visitMaxs(0,0); mv2.visitEnd();
        cw2.visitEnd();
        byte[] repl = cw2.toByteArray();

        var vr = ChangeSetValidator.validate("test.G", orig, repl);
        assertThat(vr.ok()).isFalse();
        assertThat(vr.violations()).anyMatch(v -> v.contains("field removed"));
    }
}
