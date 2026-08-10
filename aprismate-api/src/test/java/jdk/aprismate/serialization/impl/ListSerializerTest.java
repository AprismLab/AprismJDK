package jdk.aprismate.serialization.impl;

import jdk.aprismate.serialization.Serializers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ListSerializerTest {
    
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
        ListSerializer<String> serializer = new ListSerializer<>(String.class);
        assertEquals(List.class, serializer.getType());
    }
    
    @Test
    void testGetElementType() {
        ListSerializer<String> serializer = new ListSerializer<>(String.class);
        assertEquals(String.class, serializer.getElementType());
    }
    
    @Test
    void testGetFormat() {
        ListSerializer<String> serializer = new ListSerializer<>(String.class);
        assertEquals("binary", serializer.getFormat());
    }
    
    @Test
    void testSerializeDeserializeStringList() throws Exception {
        ListSerializer<String> serializer = new ListSerializer<>(String.class);
        List<String> original = Arrays.asList("one", "two", "three");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(original, baos);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        List<String> deserialized = serializer.deserialize(bais);
        
        assertEquals(original, deserialized);
    }
    
    @Test
    void testSerializeDeserializeIntegerList() throws Exception {
        ListSerializer<Integer> serializer = new ListSerializer<>(Integer.class);
        List<Integer> original = Arrays.asList(1, 2, 3, 4, 5);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(original, baos);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        List<Integer> deserialized = serializer.deserialize(bais);
        
        assertEquals(original, deserialized);
    }
    
    @Test
    void testSerializeEmptyList() throws Exception {
        ListSerializer<String> serializer = new ListSerializer<>(String.class);
        List<String> original = Arrays.asList();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(original, baos);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        List<String> deserialized = serializer.deserialize(bais);
        
        assertEquals(original, deserialized);
    }
    
    @Test
    void testDeserializeInvalidSize() {
        ListSerializer<String> serializer = new ListSerializer<>(String.class);
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[]{-1, -1, -1, -1});
        assertThrows(Exception.class, () -> serializer.deserialize(bais));
    }
    
    @Test
    void testDeserializeTooLarge() {
        ListSerializer<String> serializer = new ListSerializer<>(String.class);
        byte[] data = new byte[4];
        data[0] = 0x7F; // Large positive int
        data[1] = (byte) 0xFF;
        data[2] = (byte) 0xFF;
        data[3] = (byte) 0xFF;
        
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        assertThrows(Exception.class, () -> serializer.deserialize(bais));
    }
}
