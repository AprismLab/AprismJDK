package jdk.aprismate.serialization;

import jdk.aprismate.serialization.impl.SimpleSerializerRegistry;
import jdk.aprismate.serialization.impl.StringSerializer;
import jdk.aprismate.serialization.impl.PrimitiveSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SerializerRegistryTest {
    
    private SerializerRegistry registry;
    
    @BeforeEach
    void setUp() {
        registry = new SimpleSerializerRegistry();
    }
    
    @Test
    void testRegisterSerializer() {
        Serializer<String> serializer = new StringSerializer();
        registry.registerSerializer(serializer);
        
        assertTrue(registry.hasSerializer(String.class));
    }
    
    @Test
    void testRegisterSerializerWithType() {
        Serializer<String> serializer = new StringSerializer();
        registry.registerSerializer(String.class, serializer);
        
        assertTrue(registry.hasSerializer(String.class));
    }
    
    @Test
    void testRegisterSerializerNull() {
        assertThrows(NullPointerException.class, () -> registry.registerSerializer(null));
    }
    
    @Test
    void testGetSerializer() {
        Serializer<String> serializer = new StringSerializer();
        registry.registerSerializer(serializer);
        
        var retrieved = registry.getSerializer(String.class);
        assertTrue(retrieved.isPresent());
        assertEquals(serializer, retrieved.get());
    }
    
    @Test
    void testGetSerializerNotFound() {
        var retrieved = registry.getSerializer(String.class);
        assertFalse(retrieved.isPresent());
    }
    
    @Test
    void testGetSerializerNull() {
        assertThrows(NullPointerException.class, () -> registry.getSerializer(null));
    }
    
    @Test
    void testHasSerializer() {
        assertFalse(registry.hasSerializer(String.class));
        
        registry.registerSerializer(new StringSerializer());
        assertTrue(registry.hasSerializer(String.class));
    }
    
    @Test
    void testSerialize() throws IOException, SerializationException {
        registry.registerSerializer(new StringSerializer());
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        registry.serialize("hello", baos);
        
        assertTrue(baos.size() > 0);
    }
    
    @Test
    void testSerializeNoSerializer() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertThrows(SerializationException.class, () -> registry.serialize("hello", baos));
    }
    
    @Test
    void testSerializeNull() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertThrows(NullPointerException.class, () -> registry.serialize(null, baos));
    }
    
    @Test
    void testDeserialize() throws IOException, SerializationException {
        registry.registerSerializer(new StringSerializer());
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        registry.serialize("hello", baos);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        String result = registry.deserialize(String.class, bais);
        
        assertEquals("hello", result);
    }
    
    @Test
    void testDeserializeNoSerializer() {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        assertThrows(SerializationException.class, () -> registry.deserialize(String.class, bais));
    }
    
    @Test
    void testDeserializeNull() {
        registry.registerSerializer(new StringSerializer());
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        assertThrows(NullPointerException.class, () -> registry.deserialize(null, bais));
    }
    
    @Test
    void testGetRegisteredTypes() {
        assertTrue(registry.getRegisteredTypes().isEmpty());
        
        registry.registerSerializer(new StringSerializer());
        registry.registerSerializer(new PrimitiveSerializer<>(Integer.class));
        
        Set<Class<?>> types = registry.getRegisteredTypes();
        assertEquals(2, types.size());
        assertTrue(types.contains(String.class));
        assertTrue(types.contains(Integer.class));
    }
    
    @Test
    void testUnregisterSerializer() {
        registry.registerSerializer(new StringSerializer());
        assertTrue(registry.hasSerializer(String.class));
        
        boolean removed = registry.unregisterSerializer(String.class);
        assertTrue(removed);
        assertFalse(registry.hasSerializer(String.class));
    }
    
    @Test
    void testUnregisterSerializerNotFound() {
        boolean removed = registry.unregisterSerializer(String.class);
        assertFalse(removed);
    }
    
    @Test
    void testUnregisterSerializerNull() {
        assertThrows(NullPointerException.class, () -> registry.unregisterSerializer(null));
    }
    
    @Test
    void testClear() {
        registry.registerSerializer(new StringSerializer());
        registry.registerSerializer(new PrimitiveSerializer<>(Integer.class));
        
        assertEquals(2, registry.getRegisteredTypes().size());
        
        registry.clear();
        assertTrue(registry.getRegisteredTypes().isEmpty());
    }
    
    @Test
    void testReplaceSerializer() {
        Serializer<String> serializer1 = new StringSerializer();
        Serializer<String> serializer2 = new StringSerializer();
        
        registry.registerSerializer(serializer1);
        assertEquals(serializer1, registry.getSerializer(String.class).get());
        
        registry.registerSerializer(serializer2);
        assertEquals(serializer2, registry.getSerializer(String.class).get());
    }
}
