package aprism.agent.optimize;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Set;

/**
 * ASM core-API optimizer. Two passes over the original bytes:
 *
 * <ol>
 *   <li><b>elision</b> — void method invocations whose owner+name match
 *       the rule set are replaced by argument pops (stack-safe for void
 *       descriptors; non-void targets are never matched).</li>
 *   <li><b>probe-enter</b> — a static
 *       {@code ProbeSink.methodEnter(Ljava/lang/String;)V} call is
 *       injected at the entry of methods matching the rule set, with
 *       the tag {@code "internal.Owner.methodName"}.</li>
 * </ol>
 *
 * COMPUTE_MAXS is sufficient (we only pop what we pushed / remove
 * balanced calls); frames are recomputed via COMPUTE_FRAMES for safety
 * on class-file version 50+.
 */
public final class AsmClassOptimizer {

    private final OptimizerConfig config;

    public AsmClassOptimizer(OptimizerConfig config) {
        this.config = config;
    }

    /**
     * Transforms bytes; returns null when nothing matched (caller passes
     * original through unchanged).
     */
    public byte[] transform(String className, byte[] input) {
        if (config.isEmpty()) {
            return null;
        }
        ClassReader cr = new ClassReader(input);
        String internal = cr.getClassName();

        // Cheap pre-scan: does THIS class contain anything we would touch?
        // (elision targets live anywhere; probes target this class)
        boolean[] hit = { false };
        String dottedInternal = internal.replace('/', '.');
        cr.accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc,
                                             String signature, String[] exceptions) {
                if (!hit[0] && config.probes().stream()
                        .anyMatch(k -> k.equals(OptimizerConfig.key(dottedInternal, name)))) {
                    hit[0] = true;
                }
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String mname,
                                                String mdesc, boolean iface) {
                        if ("V".equals(Type.getReturnType(mdesc).getDescriptor())
                                && config.elisions().contains(OptimizerConfig.key(
                                        owner.replace('/', '.'), mname))) {
                            hit[0] = true;
                        }
                    }
                };
            }
        }, 0);
        if (!hit[0]) {
            return null;
        }

        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
        ClassVisitor chain = cw;
        if (matchesAny(internal, config.probes())) {
            chain = new ProbeVisitor(chain, internal, config);
        }
        if (!config.elisions().isEmpty()) {
            chain = new ElisionVisitor(chain, config);
        }
        cr.accept(chain, 0);
        return cw.toByteArray();
    }

    static boolean matchesAny(String internalName, Set<String> ruleKeys) {
        // rules use dotted owner names; convert our internal name once
        String dotted = internalName.replace('/', '.');
        for (String key : ruleKeys) {
            int lastDot = key.lastIndexOf('.');
            if (lastDot <= 0) {
                continue;
            }
            String owner = key.substring(0, lastDot);
            if (dotted.equals(owner) || dotted.startsWith(owner + "$")) {
                return true;
            }
        }
        return false;
    }

    static boolean elides(String ownerInternal, String name, OptimizerConfig cfg) {
        return cfg.elisions().contains(OptimizerConfig.key(ownerInternal, name));
    }

    static String probeTag(String internalOwner, String name) {
        return internalOwner.replace('/', '.') + "." + name;
    }

    static final class ElisionVisitor extends ClassVisitor {
        private final OptimizerConfig cfg;

        ElisionVisitor(ClassVisitor next, OptimizerConfig cfg) {
            super(Opcodes.ASM9, next);
            this.cfg = cfg;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            return new MethodVisitor(Opcodes.ASM9, mv) {
                @Override
                public void visitMethodInsn(int opcode, String owner, String mname,
                                            String mdesc, boolean isInterface) {
                    if (!"V".equals(Type.getReturnType(mdesc).getDescriptor())
                            || !cfg.elisions().contains(OptimizerConfig.key(
                                    owner.replace('/', '.'), mname))) {
                        super.visitMethodInsn(opcode, owner, mname, mdesc, isInterface);
                        return;
                    }
                    // emit argument pops (receiver already on stack for non-static)
                    Type[] args = Type.getArgumentTypes(mdesc);
                    if (opcode != Opcodes.INVOKESTATIC) {
                        super.visitInsn(Opcodes.POP); // receiver
                    }
                    for (int i = args.length - 1; i >= 0; i--) {
                        super.visitInsn(args[i].getSize() == 2
                                ? Opcodes.POP2 : Opcodes.POP);
                    }
                }
            };
        }
    }

    static final class ProbeVisitor extends ClassVisitor {
        private final String dottedOwner;
        private final OptimizerConfig cfg;

        ProbeVisitor(ClassVisitor next, String ownerInternal, OptimizerConfig cfg) {
            super(Opcodes.ASM9, next);
            this.dottedOwner = ownerInternal.replace('/', '.');
            this.cfg = cfg;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            boolean probed = cfg.probes().stream()
                    .anyMatch(k -> k.equals(OptimizerConfig.key(dottedOwner, name)));
            if (!probed || (access & Opcodes.ACC_ABSTRACT) != 0
                    || "<init>".equals(name) || "<clinit>".equals(name)) {
                return mv;
            }
            return new MethodVisitor(Opcodes.ASM9, mv) {
                @Override
                public void visitCode() {
                    super.visitCode();
                    super.visitLdcInsn(probeTag(dottedOwner, name));
                    super.visitMethodInsn(Opcodes.INVOKESTATIC,
                            "aprism/agent/optimize/ProbeSink",
                            "methodEnter", "(Ljava/lang/String;)V", false);
                }
            };
        }
    }
}
