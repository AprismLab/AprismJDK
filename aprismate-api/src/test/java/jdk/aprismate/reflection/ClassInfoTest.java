package jdk.aprismate.reflection;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ClassInfoTest {
    
    @Test
    void testGetType() {
        ClassInfo<TestClass> info = new SimpleClassInfo<>(TestClass.class);
        assertEquals(TestClass.class, info.getType());
    }
    
    @Test
    void testGetName() {
        ClassInfo<TestClass> info = new SimpleClassInfo<>(TestClass.class);
        assertEquals("TestClass", info.getName());
    }
    
    @Test
    void testGetQualifiedName() {
        ClassInfo<TestClass> info = new SimpleClassInfo<>(TestClass.class);
        assertEquals("jdk.aprismate.reflection.ClassInfoTest$TestClass", info.getQualifiedName());
    }
    
    @Test
    void testGetPackageName() {
        ClassInfo<TestClass> info = new SimpleClassInfo<>(TestClass.class);
        assertEquals("jdk.aprismate.reflection", info.getPackageName());
    }
    
    @Test
    void testGetFields() {
        ClassInfo<TestClass> info = new SimpleClassInfo<>(TestClass.class);
        List<Field> fields = info.getFields();
        
        assertTrue(fields.size() >= 2);
        assertTrue(fields.stream().anyMatch(f -> f.getName().equals("name")));
        assertTrue(fields.stream().anyMatch(f -> f.getName().equals("value")));
    }
    
    @Test
    void testGetAllFields() {
        ClassInfo<TestSubClass> info = new SimpleClassInfo<>(TestSubClass.class);
        List<Field> fields = info.getAllFields();
        
        assertTrue(fields.size() >= 3);
        assertTrue(fields.stream().anyMatch(f -> f.getName().equals("name")));
        assertTrue(fields.stream().anyMatch(f -> f.getName().equals("value")));
        assertTrue(fields.stream().anyMatch(f -> f.getName().equals("extra")));
    }
    
    @Test
    void testGetField() {
        ClassInfo<TestClass> info = new SimpleClassInfo<>(TestClass.class);
        Optional<Field> field = info.getField("name");
        
        assertTrue(field.isPresent());
        assertEquals("name", field.get().getName());
    }
    
    @Test
    void testGetFieldNotFound() {
        ClassInfo<TestClass> info = new SimpleClassInfo<>(TestClass.class);
        Optional<Field> field = info.getField("nonexistent");
        
        assertFalse(field.isPresent());
    }
    
    @Test
    void testGetMethods() {
        ClassInfo<TestClass> info = new SimpleClassInfo<>(TestClass.class);
        List<Method> methods = info.getMethods();
        
        assertTrue(methods.stream().anyMatch(m -> m.getName().equals("getName")));
        assertTrue(methods.stream().anyMatch(m -> m.getName().equals("setName")));
    }
    
    @Test
    void testGetMethod() {
        ClassInfo<TestClass> info = new SimpleClassInfo<>(TestClass.class);
        Optional<Method> method = info.getMethod("getName");
        
        assertTrue(method.isPresent());
        assertEquals("getName", method.get().getName());
    }
    
    @Test
    void testGetMethodWithParams() {
        ClassInfo<TestClass> info = new SimpleClassInfo<>(TestClass.class);
        Optional<Method> method = info.getMethod("setName", String.class);
        
        assertTrue(method.isPresent());
        assertEquals("setName", method.get().getName());
    }
    
    @Test
    void testGetConstructors() {
        ClassInfo<TestClass> info = new SimpleClassInfo<>(TestClass.class);
        List<Constructor<TestClass>> constructors = info.getConstructors();
        
        assertTrue(constructors.size() >= 2);
    }
    
    @Test
    void testGetConstructor() {
        ClassInfo<TestClass> info = new SimpleClassInfo<>(TestClass.class);
        Optional<Constructor<TestClass>> constructor = info.getConstructor();
        
        assertTrue(constructor.isPresent());
    }
    
    @Test
    void testGetConstructorWithParams() {
        ClassInfo<TestClass> info = new SimpleClassInfo<>(TestClass.class);
        Optional<Constructor<TestClass>> constructor = info.getConstructor(String.class, int.class);
        
        assertTrue(constructor.isPresent());
    }
    
    @Test
    void testNewInstance() throws Exception {
        ClassInfo<TestClass> info = new SimpleClassInfo<>(TestClass.class);
        TestClass instance = info.newInstance();
        
        assertNotNull(instance);
    }
    
    @Test
    void testNewInstanceWithArgs() throws Exception {
        ClassInfo<TestClass> info = new SimpleClassInfo<>(TestClass.class);
        TestClass instance = info.newInstance("test", 42);
        
        assertNotNull(instance);
        assertEquals("test", instance.getName());
        assertEquals(42, instance.getValue());
    }
    
    @Test
    void testGetFieldValue() throws Exception {
        ClassInfo<TestClass> info = new SimpleClassInfo<>(TestClass.class);
        TestClass instance = new TestClass("test", 42);
        
        Object value = info.getFieldValue(instance, "name");
        assertEquals("test", value);
    }
    
    @Test
    void testSetFieldValue() throws Exception {
        ClassInfo<TestClass> info = new SimpleClassInfo<>(TestClass.class);
        TestClass instance = new TestClass();
        
        info.setFieldValue(instance, "name", "updated");
        assertEquals("updated", instance.getName());
    }
    
    @Test
    void testInvokeMethod() throws Exception {
        ClassInfo<TestClass> info = new SimpleClassInfo<>(TestClass.class);
        TestClass instance = new TestClass("test", 42);
        
        Object result = info.invokeMethod(instance, "getName");
        assertEquals("test", result);
    }
    
    @Test
    void testInvokeMethodWithArgs() throws Exception {
        ClassInfo<TestClass> info = new SimpleClassInfo<>(TestClass.class);
        TestClass instance = new TestClass();
        
        info.invokeMethod(instance, "setName", "updated");
        assertEquals("updated", instance.getName());
    }
    
    @Test
    void testIsAssignableFrom() {
        ClassInfo<TestClass> info = new SimpleClassInfo<>(TestClass.class);
        assertTrue(info.isAssignableFrom(TestSubClass.class));
        assertFalse(info.isAssignableFrom(String.class));
    }
    
    @Test
    void testIsInterface() {
        ClassInfo<TestInterface> info = new SimpleClassInfo<>(TestInterface.class);
        assertTrue(info.isInterface());
        
        ClassInfo<TestClass> classInfo = new SimpleClassInfo<>(TestClass.class);
        assertFalse(classInfo.isInterface());
    }
    
    @Test
    void testIsAbstract() {
        ClassInfo<TestAbstractClass> info = new SimpleClassInfo<>(TestAbstractClass.class);
        assertTrue(info.isAbstract());
        
        ClassInfo<TestClass> classInfo = new SimpleClassInfo<>(TestClass.class);
        assertFalse(classInfo.isAbstract());
    }
    
    @Test
    void testIsEnum() {
        ClassInfo<TestEnum> info = new SimpleClassInfo<>(TestEnum.class);
        assertTrue(info.isEnum());
        
        ClassInfo<TestClass> classInfo = new SimpleClassInfo<>(TestClass.class);
        assertFalse(classInfo.isEnum());
    }
    
    @Test
    void testGetSuperclass() {
        ClassInfo<TestSubClass> info = new SimpleClassInfo<>(TestSubClass.class);
        Optional<Class<?>> superclass = info.getSuperclass();
        
        assertTrue(superclass.isPresent());
        assertEquals(TestClass.class, superclass.get());
    }
    
    @Test
    void testGetInterfaces() {
        ClassInfo<TestImplementation> info = new SimpleClassInfo<>(TestImplementation.class);
        List<Class<?>> interfaces = info.getInterfaces();
        
        assertTrue(interfaces.contains(TestInterface.class));
    }
    
    // Test classes
    
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
    
    public static class TestSubClass extends TestClass {
        private String extra;
        
        public TestSubClass() {}
        
        public String getExtra() {
            return extra;
        }
        
        public void setExtra(String extra) {
            this.extra = extra;
        }
    }
    
    public interface TestInterface {
        void doSomething();
    }
    
    public static abstract class TestAbstractClass {
        public abstract void abstractMethod();
    }
    
    public enum TestEnum {
        VALUE1, VALUE2
    }
    
    public static class TestImplementation implements TestInterface {
        @Override
        public void doSomething() {}
    }
}
