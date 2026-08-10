package jdk.aprismate.reflection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReflectTest {
    
    @Test
    void testOnClass() {
        ClassInfo<TestClass> info = Reflect.on(TestClass.class);
        
        assertNotNull(info);
        assertEquals(TestClass.class, info.getType());
    }
    
    @Test
    void testOnObject() {
        TestClass obj = new TestClass("test", 42);
        ClassInfo<TestClass> info = Reflect.on(obj);
        
        assertNotNull(info);
        assertEquals(TestClass.class, info.getType());
    }
    
    @Test
    void testOnClassName() throws Exception {
        ClassInfo<?> info = Reflect.on("java.lang.String");
        
        assertNotNull(info);
        assertEquals(String.class, info.getType());
    }
    
    @Test
    void testOnClassNameNotFound() {
        assertThrows(ReflectionException.class, () -> {
            Reflect.on("com.example.NonExistent");
        });
    }
    
    @Test
    void testNewInstance() throws Exception {
        TestClass instance = Reflect.newInstance(TestClass.class);
        
        assertNotNull(instance);
    }
    
    @Test
    void testNewInstanceWithArgs() throws Exception {
        TestClass instance = Reflect.newInstance(TestClass.class, "test", 42);
        
        assertNotNull(instance);
        assertEquals("test", instance.getName());
        assertEquals(42, instance.getValue());
    }
    
    @Test
    void testGetFieldValue() throws Exception {
        TestClass obj = new TestClass("test", 42);
        Object value = Reflect.getFieldValue(obj, "name");
        
        assertEquals("test", value);
    }
    
    @Test
    void testSetFieldValue() throws Exception {
        TestClass obj = new TestClass();
        Reflect.setFieldValue(obj, "name", "updated");
        
        assertEquals("updated", obj.getName());
    }
    
    @Test
    void testInvokeMethod() throws Exception {
        TestClass obj = new TestClass("test", 42);
        Object result = Reflect.invokeMethod(obj, "getName");
        
        assertEquals("test", result);
    }
    
    @Test
    void testInvokeMethodWithArgs() throws Exception {
        TestClass obj = new TestClass();
        Reflect.invokeMethod(obj, "setName", "updated");
        
        assertEquals("updated", obj.getName());
    }
    
    @Test
    void testCache() {
        Reflect.clearCache();
        assertEquals(0, Reflect.getCacheSize());
        
        Reflect.on(TestClass.class);
        assertEquals(1, Reflect.getCacheSize());
        
        Reflect.on(TestClass.class);
        assertEquals(1, Reflect.getCacheSize()); // Should reuse cached instance
        
        Reflect.on(String.class);
        assertEquals(2, Reflect.getCacheSize());
        
        Reflect.clearCache();
        assertEquals(0, Reflect.getCacheSize());
    }
    
    @Test
    void testNullClass() {
        assertThrows(NullPointerException.class, () -> {
            Reflect.on((Class<?>) null);
        });
    }
    
    @Test
    void testNullObject() {
        assertThrows(NullPointerException.class, () -> {
            Reflect.on((Object) null);
        });
    }
    
    @Test
    void testNullClassName() {
        assertThrows(NullPointerException.class, () -> {
            Reflect.on((String) null);
        });
    }
    
    @Test
    void testNullObjectInGetFieldValue() {
        assertThrows(NullPointerException.class, () -> {
            Reflect.getFieldValue(null, "name");
        });
    }
    
    @Test
    void testNullObjectInSetFieldValue() {
        assertThrows(NullPointerException.class, () -> {
            Reflect.setFieldValue(null, "name", "value");
        });
    }
    
    @Test
    void testNullObjectInInvokeMethod() {
        assertThrows(NullPointerException.class, () -> {
            Reflect.invokeMethod(null, "method");
        });
    }
    
    // Test class
    
    public static class TestClass {
        private String name;
        private int value;
        
        public TestClass() {}
        
        public TestClass(String name, int value) {
            this.name = name;
            this.value = value;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public int getValue() {
            return value;
        }
        
        public void setValue(int value) {
            this.value = value;
        }
    }
}
