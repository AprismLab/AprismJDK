package jdk.aprismate.serialization.impl;

import jdk.aprismate.serialization.Serializers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MapSerializerTest {
    
    @BeforeEach
    void setUp() {
        SimpleSerializerRegistry registry = new SimpleSerializerRegistry();
        registry.registerSerializer(new StringSerializer());
        registry.registerSerializer(new PrimitiveSerializer<>(Integer.class));
        Serializers.setRegistry(registry);
    }
    
    @AfterEach
    void tearDown() {
        Serializers.clear();
    }
    
    @Test
    void testGetType() {
        MapSerializer<String> serializer = new MapSerializer<>(String.class);
        assertEquals(Map.class, serializer.getType());
    }
    
    @Test
    void testGetValueType() {
        MapSerializer<String> serializer = new MapSerializer<>(String.class);
        assertEquals(String.class, serializer.getValueType());
    }
    
    @Test
    void testGetFormat() {
        MapSerializer<String> serializer = new MapSerializer<>(String.class);
        assertEquals("binary", serializer.getFormat());
    }
    
    @Test
    void testSerializeDeserializeStringMap() throws Exception {
        MapSerializer<String> serializer = new MapSerializer<>(String.class);
        Map<String, String> original = new HashMap<>();
        original.put("key1", "value1");
        original.put("key2", "value2");
        original.put("key3", "value3");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(original, baos);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Map<String, String> deserialized = serializer.deserialize(bais);
        
        assertEquals(original, deserialized);
    }
    
    @Test
    void testSerializeDeserializeIntegerMap() throws Exception {
        MapSerializer<Integer> serializer = new MapSerializer<>(Integer.class);
        Map<String, Integer> original = new HashMap<>();
        original.put("one", 1);
        original.put("two", 2);
        original.put("three", 3);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(original, baos);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Map<String, Integer> deserialized = serializer.deserialize(bais);
        
        assertEquals(original, deserialized);
    }
    
    @Test
    void testSerializeEmptyMap() throws Exception {
        MapSerializer<String> serializer = new MapSerializer<>(String.class);
        Map<String, String> original = new HashMap<>();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(original, baos);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Map<String, String> deserialized = serializer.deserialize(bais);
        
        assertEquals(original, deserialized);
    }
    
    @Test
    void testDeserializeInvalidSize() {
        MapSerializer<String> serializer = new MapSerializer<>(String.class);
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[]{-1, -1, -1, -1});
        assertThrows(Exception.class, () -> serializer.deserialize(bais));
    }
    
    @Test
    void testDeserializeTooLarge() {
        MapSerializer<String> serializer = new MapSerializer<>(String.class);
        byte[] data = new byte[4];
        data[0] = 0x7F; // Large positive int
        data[1] = (byte) 0xFF;
        data[2] = (byte) 0xFF;
        data[3] = (byte) 0xFF;
        
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        assertThrows(Exception.class, () -> serializer.deserialize(bais));
    }
}
