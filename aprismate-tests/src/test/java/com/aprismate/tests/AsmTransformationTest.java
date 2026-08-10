package com.aprismate.tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import static org.assertj.core.api.Assertions.*;

/**
 * Test suite for ASM-based bytecode transformations.
 * Demonstrates method injection, field addition, and mixin-style weaving.
 */
@DisplayName("ASM Bytecode Transformation Tests")
class AsmTransformationTest {

    @Test
    @DisplayName("Should inject method into class using ASM")
    void testMethodInjection() throws Exception {
        // Create a simple class bytecode
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "TestClass", null, "java/lang/Object", null);
        
        // Add constructor
        MethodVisitor constructor = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(Opcodes.ALOAD, 0);
        constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        constructor.visitInsn(Opcodes.RETURN);
        constructor.visitMaxs(1, 1);
        constructor.visitEnd();
        
        cw.visitEnd();
        byte[] originalBytes = cw.toByteArray();

        // Create transformer that injects a method
        ClassFileTransformer transformer = new MethodInjectingTransformer();
        byte[] transformedBytes = transformer.transform(
            null, "TestClass", null, null, originalBytes
        );

        assertThat(transformedBytes).isNotNull();
        assertThat(transformedBytes).isNotEqualTo(originalBytes);
        
        // Verify the injected method exists
        ClassReader cr = new ClassReader(transformedBytes);
        MethodCounter counter = new MethodCounter();
        cr.accept(counter, 0);
        
        // Should have constructor + injected method
        assertThat(counter.methodCount).isGreaterThan(1);
    }

    @Test
    @DisplayName("Should add field to class using ASM")
    void testFieldAddition() throws Exception {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "TestClass", null, "java/lang/Object", null);
        
        // Add constructor
        MethodVisitor constructor = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(Opcodes.ALOAD, 0);
        constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        constructor.visitInsn(Opcodes.RETURN);
        constructor.visitMaxs(1, 1);
        constructor.visitEnd();
        
        cw.visitEnd();
        byte[] originalBytes = cw.toByteArray();

        // Create transformer that adds a field
        ClassFileTransformer transformer = new FieldAddingTransformer();
        byte[] transformedBytes = transformer.transform(
            null, "TestClass", null, null, originalBytes
        );

        assertThat(transformedBytes).isNotNull();
        
        // Verify the field was added
        ClassReader cr = new ClassReader(transformedBytes);
        FieldCounter counter = new FieldCounter();
        cr.accept(counter, 0);
        
        assertThat(counter.fieldCount).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should weave multiple transformers")
    void testMultipleTransformers() throws Exception {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "TestClass", null, "java/lang/Object", null);
        
        MethodVisitor constructor = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(Opcodes.ALOAD, 0);
        constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        constructor.visitInsn(Opcodes.RETURN);
        constructor.visitMaxs(1, 1);
        constructor.visitEnd();
        
        cw.visitEnd();
        byte[] originalBytes = cw.toByteArray();

        // Apply first transformer (add field)
        ClassFileTransformer transformer1 = new FieldAddingTransformer();
        byte[] bytes1 = transformer1.transform(null, "TestClass", null, null, originalBytes);
        
        // Apply second transformer (inject method)
        ClassFileTransformer transformer2 = new MethodInjectingTransformer();
        byte[] bytes2 = transformer2.transform(null, "TestClass", null, null, bytes1);

        assertThat(bytes2).isNotNull();
        
        // Verify both transformations applied
        ClassReader cr = new ClassReader(bytes2);
        ComponentCounter counter = new ComponentCounter();
        cr.accept(counter, 0);
        
        assertThat(counter.fieldCount).isGreaterThan(0);
        assertThat(counter.methodCount).isGreaterThan(1);
    }

    @Test
    @DisplayName("Should handle transformer that returns null (no transformation)")
    void testNoTransformation() throws Exception {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "TestClass", null, "java/lang/Object", null);
        cw.visitEnd();
        byte[] originalBytes = cw.toByteArray();

        ClassFileTransformer transformer = new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className,
                                   Class<?> classBeingRedefined,
                                   ProtectionDomain protectionDomain,
                                   byte[] classfileBuffer) {
                return null;
            }
        };
        
        byte[] result = transformer.transform(null, "TestClass", null, null, originalBytes);
        
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should transform with ClassReader and ClassWriter")
    void testClassReaderWriter() throws Exception {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "TestClass", null, "java/lang/Object", null);
        
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "testMethod", "()V", null, null);
        mv.visitCode();
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 1);
        mv.visitEnd();
        
        cw.visitEnd();
        byte[] originalBytes = cw.toByteArray();

        // Read and re-write (identity transformation)
        ClassReader cr = new ClassReader(originalBytes);
        ClassWriter cw2 = new ClassWriter(0);
        cr.accept(cw2, 0);
        byte[] rewrittenBytes = cw2.toByteArray();

        assertThat(rewrittenBytes).isNotNull();
        
        // Both should have the same structure
        ClassReader cr1 = new ClassReader(originalBytes);
        ClassReader cr2 = new ClassReader(rewrittenBytes);
        
        assertThat(cr1.getClassName()).isEqualTo(cr2.getClassName());
    }

    // Helper: Transformer that injects a method
    static class MethodInjectingTransformer implements ClassFileTransformer {
        @Override
        public byte[] transform(ClassLoader loader, String className,
                              Class<?> classBeingRedefined,
                              ProtectionDomain protectionDomain,
                              byte[] classfileBuffer) {
            if (!"TestClass".equals(className)) {
                return null;
            }
            
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ClassWriter(cr, 0);
            ClassVisitor cv = new ClassVisitor(Opcodes.ASM9, cw) {
                @Override
                public void visitEnd() {
                    // Inject a new method
                    MethodVisitor mv = cv.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "injectedMethod",
                        "()Ljava/lang/String;",
                        null,
                        null
                    );
                    mv.visitCode();
                    mv.visitLdcInsn("Injected by transformer");
                    mv.visitInsn(Opcodes.ARETURN);
                    mv.visitMaxs(1, 1);
                    mv.visitEnd();
                    
                    super.visitEnd();
                }
            };
            cr.accept(cv, 0);
            return cw.toByteArray();
        }
    }

    // Helper: Transformer that adds a field
    static class FieldAddingTransformer implements ClassFileTransformer {
        @Override
        public byte[] transform(ClassLoader loader, String className,
                              Class<?> classBeingRedefined,
                              ProtectionDomain protectionDomain,
                              byte[] classfileBuffer) {
            if (!"TestClass".equals(className)) {
                return null;
            }
            
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ClassWriter(cr, 0);
            ClassVisitor cv = new ClassVisitor(Opcodes.ASM9, cw) {
                @Override
                public void visitEnd() {
                    // Add a new field
                    cv.visitField(
                        Opcodes.ACC_PRIVATE,
                        "injectedField",
                        "Ljava/lang/String;",
                        null,
                        null
                    ).visitEnd();
                    
                    super.visitEnd();
                }
            };
            cr.accept(cv, 0);
            return cw.toByteArray();
        }
    }

    // Helper visitor to count methods
    static class MethodCounter extends ClassVisitor {
        int methodCount = 0;
        
        MethodCounter() {
            super(Opcodes.ASM9);
        }
        
        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                        String signature, String[] exceptions) {
            methodCount++;
            return null;
        }
    }

    // Helper visitor to count fields
    static class FieldCounter extends ClassVisitor {
        int fieldCount = 0;
        
        FieldCounter() {
            super(Opcodes.ASM9);
        }
        
        @Override
        public FieldVisitor visitField(int access, String name, String descriptor,
                                      String signature, Object value) {
            fieldCount++;
            return null;
        }
    }

    // Helper visitor to count both
    static class ComponentCounter extends ClassVisitor {
        int methodCount = 0;
        int fieldCount = 0;
        
        ComponentCounter() {
            super(Opcodes.ASM9);
        }
        
        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                        String signature, String[] exceptions) {
            methodCount++;
            return null;
        }
        
        @Override
        public FieldVisitor visitField(int access, String name, String descriptor,
                                      String signature, Object value) {
            fieldCount++;
            return null;
        }
    }
}
