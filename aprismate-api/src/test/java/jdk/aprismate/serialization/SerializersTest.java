package jdk.aprismate.serialization;

import jdk.aprismate.serialization.impl.SimpleSerializerRegistry;
import jdk.aprismate.serialization.impl.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SerializersTest {
    
    @AfterEach
    void tearDown() {
        Serializers.clear();
    }
    
    @Test
    void testSetRegistry() {
        SerializerRegistry registry = new SimpleSerializerRegistry();
        Serializers.setRegistry(registry);
        
        assertEquals(registry, Serializers.getRegistry());
    }
    
    @Test
    void testSetRegistryNull() {
        assertThrows(NullPointerException.class, () -> Serializers.setRegistry(null));
    }
    
    @Test
    void testSetRegistryTwice() {
        SerializerRegistry registry1 = new SimpleSerializerRegistry();
        SerializerRegistry registry2 = new SimpleSerializerRegistry();
        
        Serializers.setRegistry(registry1);
        assertThrows(IllegalStateException.class, () -> Serializers.setRegistry(registry2));
    }
    
    @Test
    void testGetRegistryNotInitialized() {
        assertThrows(IllegalStateException.class, () -> Serializers.getRegistry());
    }
    
    @Test
    void testIsInitialized() {
        assertFalse(Serializers.isInitialized());
        
        Serializers.setRegistry(new SimpleSerializerRegistry());
        assertTrue(Serializers.isInitialized());
    }
    
    @Test
    void testClear() {
        Serializers.setRegistry(new SimpleSerializerRegistry());
        assertTrue(Serializers.isInitialized());
        
        Serializers.clear();
        assertFalse(Serializers.isInitialized());
    }
    
    @Test
    void testCannotInstantiate() throws Exception {
        var constructor = Serializers.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        
        var exception = assertThrows(java.lang.reflect.InvocationTargetException.class, 
            () -> constructor.newInstance());
        
        assertTrue(exception.getCause() instanceof UnsupportedOperationException);
        assertEquals("Cannot instantiate Serializers", exception.getCause().getMessage());
    }
}
