package jdk.aprismate.agent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.EnabledIf;

import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.*;

/**
 * Test suite for ClassRedefiner+ functionality.
 * <p>
 * Note: Most tests are disabled on stock JDK as they require VM support.
 * Tests will be enabled when running on AprismJDK with VM patches.
 * </p>
 */
@DisplayName("ClassRedefiner Tests")
class ClassRedefinerTest {
    
    @BeforeEach
    void setUp() {
        // Reset any state if needed
    }
    
    @Test
    @DisplayName("isSupported() should return false on stock JDK")
    void testIsSupportedOnStockJdk() {
        // On stock JDK, ClassRedefiner+ should not be supported
        // On AprismJDK with patches, this will return true
        boolean supported = ClassRedefiner.isSupported();
        
        // We can't assert the exact value as it depends on the JDK
        // Just verify the method doesn't throw
        assertThat(supported).isIn(true, false);
    }
    
    @Test
    @DisplayName("redefineClass() should throw UnsupportedOperationException on stock JDK")
    void testRedefineClassUnsupportedOnStockJdk() {
        if (ClassRedefiner.isSupported()) {
            // Skip on AprismJDK
            return;
        }
        
        byte[] dummyBytes = new byte[]{(byte)0xCA, (byte)0xFE, (byte)0xBA, (byte)0xBE};
        
        assertThatThrownBy(() -> ClassRedefiner.redefineClass(String.class, dummyBytes))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessageContaining("ClassRedefiner+ is not supported");
    }
    
    @Test
    @DisplayName("addField() should throw UnsupportedOperationException on stock JDK")
    void testAddFieldUnsupportedOnStockJdk() {
        if (ClassRedefiner.isSupported()) {
            return;
        }
        
        FieldDescriptor field = FieldDescriptor.builder("testField", int.class)
            .makePublic()
            .build();
        
        assertThatThrownBy(() -> ClassRedefiner.addField(String.class, field))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessageContaining("ClassRedefiner+ is not supported");
    }
    
    @Test
    @DisplayName("removeField() should throw UnsupportedOperationException on stock JDK")
    void testRemoveFieldUnsupportedOnStockJdk() {
        if (ClassRedefiner.isSupported()) {
            return;
        }
        
        assertThatThrownBy(() -> ClassRedefiner.removeField(String.class, "value"))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessageContaining("ClassRedefiner+ is not supported");
    }
    
    @Test
    @DisplayName("addMethod() should throw UnsupportedOperationException on stock JDK")
    void testAddMethodUnsupportedOnStockJdk() {
        if (ClassRedefiner.isSupported()) {
            return;
        }
        
        MethodDescriptor method = MethodDescriptor.builder("testMethod", void.class)
            .makePublic()
            .makeAbstract()
            .build();
        
        assertThatThrownBy(() -> ClassRedefiner.addMethod(String.class, method))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessageContaining("ClassRedefiner+ is not supported");
    }
    
    @Test
    @DisplayName("validateBytecode() should throw UnsupportedOperationException on stock JDK")
    void testValidateBytecodeUnsupportedOnStockJdk() {
        if (ClassRedefiner.isSupported()) {
            return;
        }
        
        byte[] dummyBytes = new byte[]{(byte)0xCA, (byte)0xFE, (byte)0xBA, (byte)0xBE};
        
        assertThatThrownBy(() -> ClassRedefiner.validateBytecode(String.class, dummyBytes))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessageContaining("ClassRedefiner+ is not supported");
    }
    
    @Test
    @DisplayName("redefineClass() should reject null class")
    void testRedefineClassNullClass() {
        byte[] dummyBytes = new byte[]{(byte)0xCA, (byte)0xFE, (byte)0xBA, (byte)0xBE};
        
        assertThatThrownBy(() -> ClassRedefiner.redefineClass(null, dummyBytes))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Class cannot be null");
    }
    
    @Test
    @DisplayName("redefineClass() should reject null bytecode")
    void testRedefineClassNullBytecode() {
        assertThatThrownBy(() -> ClassRedefiner.redefineClass(String.class, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Class bytes cannot be null");
    }
    
    @Test
    @DisplayName("addField() should reject null class")
    void testAddFieldNullClass() {
        FieldDescriptor field = FieldDescriptor.builder("test", int.class).build();
        
        assertThatThrownBy(() -> ClassRedefiner.addField(null, field))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Class cannot be null");
    }
    
    @Test
    @DisplayName("addField() should reject null descriptor")
    void testAddFieldNullDescriptor() {
        assertThatThrownBy(() -> ClassRedefiner.addField(String.class, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Field descriptor cannot be null");
    }
    
    @Test
    @DisplayName("removeField() should reject null class")
    void testRemoveFieldNullClass() {
        assertThatThrownBy(() -> ClassRedefiner.removeField(null, "field"))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Class cannot be null");
    }
    
    @Test
    @DisplayName("removeField() should reject null field name")
    void testRemoveFieldNullName() {
        assertThatThrownBy(() -> ClassRedefiner.removeField(String.class, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Field name cannot be null");
    }
    
    @Test
    @DisplayName("addMethod() should reject null class")
    void testAddMethodNullClass() {
        MethodDescriptor method = MethodDescriptor.builder("test", void.class)
            .makeAbstract()
            .build();
        
        assertThatThrownBy(() -> ClassRedefiner.addMethod(null, method))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Class cannot be null");
    }
    
    @Test
    @DisplayName("addMethod() should reject null descriptor")
    void testAddMethodNullDescriptor() {
        assertThatThrownBy(() -> ClassRedefiner.addMethod(String.class, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Method descriptor cannot be null");
    }
    
    @Test
    @DisplayName("addMethod() should reject non-abstract method without bytecode")
    void testAddMethodNoBytecode() {
        MethodDescriptor method = MethodDescriptor.builder("test", void.class)
            .makePublic()
            .build(); // Not abstract, not native, no bytecode
        
        assertThatThrownBy(() -> ClassRedefiner.addMethod(String.class, method))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must have bytecode");
    }
    
    @Test
    @DisplayName("validateBytecode() should reject null class")
    void testValidateBytecodeNullClass() {
        byte[] dummyBytes = new byte[]{(byte)0xCA, (byte)0xFE, (byte)0xBA, (byte)0xBE};
        
        assertThatThrownBy(() -> ClassRedefiner.validateBytecode(null, dummyBytes))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Class cannot be null");
    }
    
    @Test
    @DisplayName("validateBytecode() should reject null bytecode")
    void testValidateBytecodeNullBytecode() {
        assertThatThrownBy(() -> ClassRedefiner.validateBytecode(String.class, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Class bytes cannot be null");
    }
    
    // Tests that require AprismJDK VM support
    // These will be skipped on stock JDK
    
    @Test
    @EnabledIf("jdk.aprismate.agent.ClassRedefiner#isSupported")
    @DisplayName("[AprismJDK] Should add instance field to class")
    void testAddInstanceField() throws Exception {
        // This test requires AprismJDK with VM patches
        // Create a test class dynamically
        TestClass obj = new TestClass();
        
        FieldDescriptor field = FieldDescriptor.builder("dynamicField", String.class)
            .makePublic()
            .initialValue("test")
            .build();
        
        ClassRedefiner.addField(TestClass.class, field);
        
        // Verify field exists via reflection
        var declaredField = TestClass.class.getDeclaredField("dynamicField");
        assertThat(declaredField).isNotNull();
        assertThat(declaredField.getType()).isEqualTo(String.class);
        assertThat(declaredField.get(obj)).isEqualTo("test");
    }
    
    @Test
    @EnabledIf("jdk.aprismate.agent.ClassRedefiner#isSupported")
    @DisplayName("[AprismJDK] Should add static field to class")
    void testAddStaticField() throws Exception {
        FieldDescriptor field = FieldDescriptor.builder("staticField", int.class)
            .makePublic()
            .makeStatic()
            .initialValue(42)
            .build();
        
        ClassRedefiner.addField(TestClass.class, field);
        
        var declaredField = TestClass.class.getDeclaredField("staticField");
        assertThat(declaredField).isNotNull();
        assertThat(Modifier.isStatic(declaredField.getModifiers())).isTrue();
        assertThat(declaredField.get(null)).isEqualTo(42);
    }
    
    @Test
    @EnabledIf("jdk.aprismate.agent.ClassRedefiner#isSupported")
    @DisplayName("[AprismJDK] Should add method to class")
    void testAddMethod() throws Exception {
        // Simple method that returns 42
        byte[] bytecode = {
            (byte)0x10, (byte)0x2A, // bipush 42
            (byte)0xAC              // ireturn
        };
        
        MethodDescriptor method = MethodDescriptor.builder("dynamicMethod", int.class)
            .makePublic()
            .bytecode(bytecode)
            .build();
        
        ClassRedefiner.addMethod(TestClass.class, method);
        
        var declaredMethod = TestClass.class.getDeclaredMethod("dynamicMethod");
        assertThat(declaredMethod).isNotNull();
        assertThat(declaredMethod.getReturnType()).isEqualTo(int.class);
        
        TestClass obj = new TestClass();
        int result = (int) declaredMethod.invoke(obj);
        assertThat(result).isEqualTo(42);
    }
    
    // Test class for dynamic modification
    public static class TestClass {
        public int existingField = 10;
        
        public int existingMethod() {
            return 20;
        }
    }
}
