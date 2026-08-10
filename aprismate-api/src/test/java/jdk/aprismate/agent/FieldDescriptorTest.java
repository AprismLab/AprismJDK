package jdk.aprismate.agent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FieldDescriptor Tests")
class FieldDescriptorTest {
    
    @Test
    @DisplayName("Should create basic field descriptor")
    void testBasicFieldDescriptor() {
        FieldDescriptor field = FieldDescriptor.builder("testField", int.class)
            .build();
        
        assertThat(field.getName()).isEqualTo("testField");
        assertThat(field.getType()).isEqualTo(int.class);
        assertThat(field.getModifiers()).isEqualTo(0);
        assertThat(field.getInitialValue()).isNull();
    }
    
    @Test
    @DisplayName("Should create public field")
    void testPublicField() {
        FieldDescriptor field = FieldDescriptor.builder("publicField", String.class)
            .makePublic()
            .build();
        
        assertThat(field.isPublic()).isTrue();
        assertThat(field.isPrivate()).isFalse();
        assertThat(field.isProtected()).isFalse();
    }
    
    @Test
    @DisplayName("Should create private field")
    void testPrivateField() {
        FieldDescriptor field = FieldDescriptor.builder("privateField", String.class)
            .makePrivate()
            .build();
        
        assertThat(field.isPrivate()).isTrue();
        assertThat(field.isPublic()).isFalse();
    }
    
    @Test
    @DisplayName("Should create static field")
    void testStaticField() {
        FieldDescriptor field = FieldDescriptor.builder("staticField", int.class)
            .makeStatic()
            .build();
        
        assertThat(field.isStatic()).isTrue();
    }
    
    @Test
    @DisplayName("Should create final field")
    void testFinalField() {
        FieldDescriptor field = FieldDescriptor.builder("finalField", String.class)
            .makeFinal()
            .build();
        
        assertThat(field.isFinal()).isTrue();
    }
    
    @Test
    @DisplayName("Should create volatile field")
    void testVolatileField() {
        FieldDescriptor field = FieldDescriptor.builder("volatileField", int.class)
            .makeVolatile()
            .build();
        
        assertThat(field.getModifiers() & Modifier.VOLATILE).isNotEqualTo(0);
    }
    
    @Test
    @DisplayName("Should create transient field")
    void testTransientField() {
        FieldDescriptor field = FieldDescriptor.builder("transientField", String.class)
            .makeTransient()
            .build();
        
        assertThat(field.getModifiers() & Modifier.TRANSIENT).isNotEqualTo(0);
    }
    
    @Test
    @DisplayName("Should set initial value for reference type")
    void testInitialValueReference() {
        String initialValue = "test";
        FieldDescriptor field = FieldDescriptor.builder("stringField", String.class)
            .initialValue(initialValue)
            .build();
        
        assertThat(field.getInitialValue()).isEqualTo(initialValue);
    }
    
    @Test
    @DisplayName("Should set initial value for primitive type")
    void testInitialValuePrimitive() {
        FieldDescriptor field = FieldDescriptor.builder("intField", int.class)
            .initialValue(42)
            .build();
        
        assertThat(field.getInitialValue()).isEqualTo(42);
    }
    
    @Test
    @DisplayName("Should accept null initial value")
    void testNullInitialValue() {
        FieldDescriptor field = FieldDescriptor.builder("nullField", String.class)
            .initialValue(null)
            .build();
        
        assertThat(field.getInitialValue()).isNull();
    }
    
    @Test
    @DisplayName("Should reject null field name")
    void testNullFieldName() {
        assertThatThrownBy(() -> FieldDescriptor.builder(null, int.class))
            .isInstanceOf(NullPointerException.class);
    }
    
    @Test
    @DisplayName("Should reject null field type")
    void testNullFieldType() {
        assertThatThrownBy(() -> FieldDescriptor.builder("field", null))
            .isInstanceOf(NullPointerException.class);
    }
    
    @Test
    @DisplayName("Should reject empty field name")
    void testEmptyFieldName() {
        assertThatThrownBy(() -> FieldDescriptor.builder("", int.class).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be empty");
    }
    
    @Test
    @DisplayName("Should reject invalid field name")
    void testInvalidFieldName() {
        assertThatThrownBy(() -> FieldDescriptor.builder("123invalid", int.class).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid field name");
    }
    
    @Test
    @DisplayName("Should reject type mismatch for initial value")
    void testTypeMismatch() {
        assertThatThrownBy(() -> 
            FieldDescriptor.builder("field", int.class)
                .initialValue("string")
                .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("type mismatch");
    }
    
    @Test
    @DisplayName("Should accept correct wrapper for primitive")
    void testPrimitiveWrapper() {
        FieldDescriptor field = FieldDescriptor.builder("intField", int.class)
            .initialValue(Integer.valueOf(42))
            .build();
        
        assertThat(field.getInitialValue()).isEqualTo(42);
    }
    
    @Test
    @DisplayName("Should support multiple modifiers")
    void testMultipleModifiers() {
        FieldDescriptor field = FieldDescriptor.builder("field", int.class)
            .makePublic()
            .makeStatic()
            .makeFinal()
            .build();
        
        assertThat(field.isPublic()).isTrue();
        assertThat(field.isStatic()).isTrue();
        assertThat(field.isFinal()).isTrue();
    }
    
    @Test
    @DisplayName("Should support custom modifiers")
    void testCustomModifiers() {
        int modifiers = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
        FieldDescriptor field = FieldDescriptor.builder("field", int.class)
            .modifiers(modifiers)
            .build();
        
        assertThat(field.getModifiers()).isEqualTo(modifiers);
    }
    
    @Test
    @DisplayName("Should implement equals correctly")
    void testEquals() {
        FieldDescriptor field1 = FieldDescriptor.builder("field", int.class)
            .makePublic()
            .initialValue(42)
            .build();
        
        FieldDescriptor field2 = FieldDescriptor.builder("field", int.class)
            .makePublic()
            .initialValue(42)
            .build();
        
        assertThat(field1).isEqualTo(field2);
        assertThat(field1.hashCode()).isEqualTo(field2.hashCode());
    }
    
    @Test
    @DisplayName("Should implement toString")
    void testToString() {
        FieldDescriptor field = FieldDescriptor.builder("testField", int.class)
            .makePublic()
            .makeStatic()
            .build();
        
        String str = field.toString();
        assertThat(str).contains("testField");
        assertThat(str).contains("int");
        assertThat(str).contains("public static");
    }
}
