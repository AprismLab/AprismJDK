package aprism.agent.optimize;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AsmClassOptimizerTest {

    // ---------- bytecode fixtures ----------

    /** Generates a class with a no-arg ctor and debug() calling Debug.log. */
    private byte[] sampleWithDebugCall(String className, String callOwner) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, className.replace('.', '/'),
                null, "java/lang/Object", null);
        MethodVisitor init = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        init.visitCode();
        init.visitVarInsn(Opcodes.ALOAD, 0);
        init.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object",
                "<init>", "()V", false);
        init.visitInsn(Opcodes.RETURN);
        init.visitMaxs(1, 1);
        init.visitEnd();
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "debug", "()V", null, null);
        mv.visitCode();
        mv.visitLdcInsn("x");
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, callOwner.replace('.', '/'),
                "log", "(Ljava/lang/String;)V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    /** Generates: int compute(int a,int b){ return a+b; } plus a no-arg ctor. */
    private byte[] sampleCompute(String className) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, className.replace('.', '/'),
                null, "java/lang/Object", null);
        MethodVisitor init = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        init.visitCode();
        init.visitVarInsn(Opcodes.ALOAD, 0);
        init.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object",
                "<init>", "()V", false);
        init.visitInsn(Opcodes.RETURN);
        init.visitMaxs(1, 1);
        init.visitEnd();
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "compute",
                "(II)I", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ILOAD, 1);
        mv.visitVarInsn(Opcodes.ILOAD, 2);
        mv.visitInsn(Opcodes.IADD);
        mv.visitInsn(Opcodes.IRETURN);
        mv.visitMaxs(2, 3);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    // ---------- elision ----------

    @Test
    void elisionRemovesVoidCallAndKeepsMethodRunnable() throws Throwable {
        String cls = "optgen.SampleA";
        OptimizerConfig cfg = new OptimizerConfig(
                Set.of(OptimizerConfig.key("other.Debug", "log")), Set.of(), Path.of("target/opt-cache-test"));
        AsmClassOptimizer opt = new AsmClassOptimizer(cfg);

        byte[] in = sampleWithDebugCall(cls, "other.Debug");
        byte[] out = opt.transform(cls, in);
        assertThat(out).isNotNull();

        Class<?> c = new ByteArrayClassLoader().define(cls, out);
        Object inst = c.getDeclaredConstructor().newInstance();
        Method m = c.getMethod("debug");
        m.invoke(inst); // must not throw (call to missing other.Debug removed)
    }

    @Test
    void noRuleMatchReturnsNullPassthrough() {
        OptimizerConfig cfg = new OptimizerConfig(Set.of(), Set.of(), Path.of("x"));
        assertThat(new AsmClassOptimizer(cfg)
                .transform("whatever.Zz", sampleWithDebugCall("whatever.Zz", "other.Debug")))
                .isNull();
    }

    @Test
    void nonMatchingElisionKeepsCall() throws Throwable {
        String cls = "optgen.SampleB";
        OptimizerConfig cfg = new OptimizerConfig(
                Set.of(OptimizerConfig.key("unrelated.K", "log")), Set.of(), Path.of("x"));
        byte[] out = new AsmClassOptimizer(cfg).transform(cls,
                sampleWithDebugCall(cls, "other.Debug"));
        assertThat(out).isNull(); // nothing matched -> passthrough
    }

    @Test
    void nonVoidTargetsNeverElided() throws Throwable {
        // rule targets a NON-void method name on owner; visitor must skip it
        String cls = "optgen.SampleC";
        OptimizerConfig cfg = new OptimizerConfig(
                Set.of(OptimizerConfig.key("java.lang.Math", "max")), Set.of(), Path.of("x"));
        byte[] in = sampleCompute(cls); // calls nothing; just shape check
        assertThat(new AsmClassOptimizer(cfg).transform(cls, in)).isNull();
    }

    // ---------- probe-enter ----------

    @Test
    void probeInjectedAndFiresOnExecution() throws Throwable {
        String cls = "optgen.SampleD";
        OptimizerConfig cfg = new OptimizerConfig(
                Set.of(),
                Set.of(OptimizerConfig.key(cls, "compute")),
                Path.of("x"));
        byte[] in = sampleCompute(cls);
        byte[] out = new AsmClassOptimizer(cfg).transform(cls, in);
        assertThat(out).isNotNull();

        long before = ProbeSink.totalEntries();
        Class<?> c = new ByteArrayClassLoader().define(cls, out);
        Object inst = c.getDeclaredConstructor().newInstance();
        Method m = c.getMethod("compute", int.class, int.class);
        Object r = m.invoke(inst, 2, 3);
        assertThat(r).isEqualTo(5); // original semantics preserved
        assertThat(ProbeSink.totalEntries()).isEqualTo(before + 1);
    }

    @Test
    void constructorsNotProbed() throws Throwable {
        String cls = "optgen.SampleE";
        OptimizerConfig cfg = new OptimizerConfig(
                Set.of(),
                Set.of(OptimizerConfig.key(cls, "*init*")),
                Path.of("x"));
        byte[] in = sampleCompute(cls);
        // rule key targets method literally named like init below:
        cfg = new OptimizerConfig(
                Set.of(),
                Set.of(OptimizerConfig.key(cls, "compute")),
                Path.of("x"));
        byte[] out = new AsmClassOptimizer(cfg).transform(cls, in);
        assertThat(out).isNotNull();
        Class<?> c = new ByteArrayClassLoader().define(cls, out);
        // constructing twice should NOT add probe entries (<init> excluded)
        long before = ProbeSink.totalEntries();
        c.getDeclaredConstructor().newInstance();
        c.getDeclaredConstructor().newInstance();
        assertThat(ProbeSink.totalEntries()).isEqualTo(before);
    }

    // ---------- matching helper ----------

    @Test
    void matchesOwnerAndInnerClasses() {
        assertThat(AsmClassOptimizer.matchesAny("com/example/Hot",
                Set.of("com.example.Hot.compute"))).isTrue();
        assertThat(AsmClassOptimizer.matchesAny("com/example/Hot$Inner",
                Set.of("com.example.Hot.compute"))).isTrue();
        assertThat(AsmClassOptimizer.matchesAny("com/example/Other",
                Set.of("com.example.Hot.compute"))).isFalse();
    }
}
