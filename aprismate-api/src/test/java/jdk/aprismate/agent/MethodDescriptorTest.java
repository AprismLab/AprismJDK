package jdk.aprismate.agent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.reflect.Modifier;
import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MethodDescriptor Tests")
class MethodDescriptorTest {
    
    @Test
    @DisplayName("Should create basic method descriptor")
    void testBasicMethodDescriptor() {
        MethodDescriptor method = MethodDescriptor.builder("testMethod", void.class)
            .build();
        
        assertThat(method.getName()).isEqualTo("testMethod");
        assertThat(method.getReturnType()).isEqualTo(void.class);
        assertThat(method.getParameterTypes()).isEmpty();
        assertThat(method.getModifiers()).isEqualTo(0);
        assertThat(method.getBytecode()).isNull();
    }
    
    @Test
    @DisplayName("Should create method with parameters")
    void testMethodWithParameters() {
        MethodDescriptor method = MethodDescriptor.builder("calculate", int.class)
            .parameterTypes(int.class, String.class, double.class)
            .build();
        
        assertThat(method.getParameterTypes()).containsExactly(int.class, String.class, double.class);
    }
    
    @Test
    @DisplayName("Should create public method")
    void testPublicMethod() {
        MethodDescriptor method = MethodDescriptor.builder("publicMethod", void.class)
            .makePublic()
            .build();
        
        assertThat(method.isPublic()).isTrue();
        assertThat(method.isPrivate()).isFalse();
        assertThat(method.isProtected()).isFalse();
    }
    
    @Test
    @DisplayName("Should create private method")
    void testPrivateMethod() {
        MethodDescriptor method = MethodDescriptor.builder("privateMethod", void.class)
            .makePrivate()
            .build();
        
        assertThat(method.isPrivate()).isTrue();
        assertThat(method.isPublic()).isFalse();
    }
    
    @Test
    @DisplayName("Should create static method")
    void testStaticMethod() {
        MethodDescriptor method = MethodDescriptor.builder("staticMethod", void.class)
            .makeStatic()
            .build();
        
        assertThat(method.isStatic()).isTrue();
    }
    
    @Test
    @DisplayName("Should create final method")
    void testFinalMethod() {
        MethodDescriptor method = MethodDescriptor.builder("finalMethod", void.class)
            .makeFinal()
            .build();
        
        assertThat(method.isFinal()).isTrue();
    }
    
    @Test
    @DisplayName("Should create abstract method")
    void testAbstractMethod() {
        MethodDescriptor method = MethodDescriptor.builder("abstractMethod", void.class)
            .makeAbstract()
            .build();
        
        assertThat(method.isAbstract()).isTrue();
    }
    
    @Test
    @DisplayName("Should create native method")
    void testNativeMethod() {
        MethodDescriptor method = MethodDescriptor.builder("nativeMethod", void.class)
            .makeNative()
            .build();
        
        assertThat(method.isNative()).isTrue();
    }
    
    @Test
    @DisplayName("Should create synchronized method")
    void testSynchronizedMethod() {
        MethodDescriptor method = MethodDescriptor.builder("synchronizedMethod", void.class)
            .makeSynchronized()
            .build();
        
        assertThat(method.isSynchronized()).isTrue();
    }
    
    @Test
    @DisplayName("Should set bytecode")
    void testBytecode() {
        byte[] bytecode = {0x01, 0x02, 0x03};
        MethodDescriptor method = MethodDescriptor.builder("method", void.class)
            .bytecode(bytecode)
            .build();
        
        assertThat(method.getBytecode()).isEqualTo(bytecode);
        // Verify defensive copy
        bytecode[0] = (byte) 0x99;
        assertThat(method.getBytecode()[0]).isEqualTo((byte) 0x01);
    }
    
    @Test
    @DisplayName("Should set exception types")
    void testExceptionTypes() {
        MethodDescriptor method = MethodDescriptor.builder("method", void.class)
            .exceptionTypes(IOException.class, IllegalArgumentException.class)
            .build();
        
        assertThat(method.getExceptionTypes()).containsExactly(
            IOException.class, IllegalArgumentException.class);
    }
    
    @Test
    @DisplayName("Should generate correct descriptor for void method")
    void testDescriptorVoid() {
        MethodDescriptor method = MethodDescriptor.builder("method", void.class)
            .build();
        
        assertThat(method.getDescriptor()).isEqualTo("()V");
    }
    
    @Test
    @DisplayName("Should generate correct descriptor for primitive parameters")
    void testDescriptorPrimitives() {
        MethodDescriptor method = MethodDescriptor.builder("method", int.class)
            .parameterTypes(int.class, long.class, boolean.class)
            .build();
        
        assertThat(method.getDescriptor()).isEqualTo("(IJZ)I");
    }
    
    @Test
    @DisplayName("Should generate correct descriptor for reference parameters")
    void testDescriptorReferences() {
        MethodDescriptor method = MethodDescriptor.builder("method", String.class)
            .parameterTypes(String.class, Object.class)
            .build();
        
        assertThat(method.getDescriptor()).isEqualTo("(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/String;");
    }
    
    @Test
    @DisplayName("Should generate correct descriptor for array parameters")
    void testDescriptorArrays() {
        MethodDescriptor method = MethodDescriptor.builder("method", void.class)
            .parameterTypes(int[].class, String[].class)
            .build();
        
        assertThat(method.getDescriptor()).isEqualTo("([I[Ljava/lang/String;)V");
    }
    
    @Test
    @DisplayName("Should generate correct descriptor for all primitive types")
    void testDescriptorAllPrimitives() {
        MethodDescriptor method = MethodDescriptor.builder("method", void.class)
            .parameterTypes(
                boolean.class, byte.class, char.class, short.class,
                int.class, long.class, float.class, double.class)
            .build();
        
        assertThat(method.getDescriptor()).isEqualTo("(ZBCSIJFD)V");
    }
    
    @Test
    @DisplayName("Should reject null method name")
    void testNullMethodName() {
        assertThatThrownBy(() -> MethodDescriptor.builder(null, void.class))
            .isInstanceOf(NullPointerException.class);
    }
    
    @Test
    @DisplayName("Should reject null return type")
    void testNullReturnType() {
        assertThatThrownBy(() -> MethodDescriptor.builder("method", null))
            .isInstanceOf(NullPointerException.class);
    }
    
    @Test
    @DisplayName("Should reject empty method name")
    void testEmptyMethodName() {
        assertThatThrownBy(() -> MethodDescriptor.builder("", void.class).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be empty");
    }
    
    @Test
    @DisplayName("Should reject invalid method name")
    void testInvalidMethodName() {
        assertThatThrownBy(() -> MethodDescriptor.builder("123invalid", void.class).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid method name");
    }
    
    @Test
    @DisplayName("Should accept special method names")
    void testSpecialMethodNames() {
        MethodDescriptor init = MethodDescriptor.builder("<init>", void.class).build();
        assertThat(init.getName()).isEqualTo("<init>");
        
        MethodDescriptor clinit = MethodDescriptor.builder("<clinit>", void.class).build();
        assertThat(clinit.getName()).isEqualTo("<clinit>");
    }
    
    @Test
    @DisplayName("Should reject null parameter type")
    void testNullParameterType() {
        assertThatThrownBy(() -> 
            MethodDescriptor.builder("method", void.class)
                .parameterTypes(int.class, null, String.class)
                .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Parameter type at index");
    }
    
    @Test
    @DisplayName("Should reject non-Throwable exception type")
    void testInvalidExceptionType() {
        assertThatThrownBy(() -> 
            MethodDescriptor.builder("method", void.class)
                .exceptionTypes(String.class)
                .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must extend Throwable");
    }
    
    @Test
    @DisplayName("Should reject null exception type")
    void testNullExceptionType() {
        assertThatThrownBy(() -> 
            MethodDescriptor.builder("method", void.class)
                .exceptionTypes(IOException.class, null)
                .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Exception type at index");
    }
    
    @Test
    @DisplayName("Should support multiple modifiers")
    void testMultipleModifiers() {
        MethodDescriptor method = MethodDescriptor.builder("method", void.class)
            .makePublic()
            .makeStatic()
            .makeFinal()
            .build();
        
        assertThat(method.isPublic()).isTrue();
        assertThat(method.isStatic()).isTrue();
        assertThat(method.isFinal()).isTrue();
    }
    
    @Test
    @DisplayName("Should support custom modifiers")
    void testCustomModifiers() {
        int modifiers = Modifier.PUBLIC | Modifier.STATIC | Modifier.SYNCHRONIZED;
        MethodDescriptor method = MethodDescriptor.builder("method", void.class)
            .modifiers(modifiers)
            .build();
        
        assertThat(method.getModifiers()).isEqualTo(modifiers);
    }
    
    @Test
    @DisplayName("Should implement equals correctly")
    void testEquals() {
        byte[] bytecode = {0x01, 0x02};
        
        MethodDescriptor method1 = MethodDescriptor.builder("method", int.class)
            .parameterTypes(String.class)
            .makePublic()
            .bytecode(bytecode)
            .build();
        
        MethodDescriptor method2 = MethodDescriptor.builder("method", int.class)
            .parameterTypes(String.class)
            .makePublic()
            .bytecode(bytecode)
            .build();
        
        assertThat(method1).isEqualTo(method2);
        assertThat(method1.hashCode()).isEqualTo(method2.hashCode());
    }
    
    @Test
    @DisplayName("Should implement toString")
    void testToString() {
        MethodDescriptor method = MethodDescriptor.builder("testMethod", int.class)
            .parameterTypes(String.class, int.class)
            .makePublic()
            .makeStatic()
            .build();
        
        String str = method.toString();
        assertThat(str).contains("testMethod");
        assertThat(str).contains("int");
        assertThat(str).contains("public static");
    }
    
    @Test
    @DisplayName("Should handle null parameter types array")
    void testNullParameterTypesArray() {
        MethodDescriptor method = MethodDescriptor.builder("method", void.class)
            .parameterTypes((Class<?>[]) null)
            .build();
        
        assertThat(method.getParameterTypes()).isEmpty();
    }
    
    @Test
    @DisplayName("Should handle null exception types array")
    void testNullExceptionTypesArray() {
        MethodDescriptor method = MethodDescriptor.builder("method", void.class)
            .exceptionTypes((Class<?>[]) null)
            .build();
        
        assertThat(method.getExceptionTypes()).isEmpty();
    }
    
    @Test
    @DisplayName("Should handle null bytecode array")
    void testNullBytecodeArray() {
        MethodDescriptor method = MethodDescriptor.builder("method", void.class)
            .bytecode(null)
            .build();
        
        assertThat(method.getBytecode()).isNull();
    }
}
