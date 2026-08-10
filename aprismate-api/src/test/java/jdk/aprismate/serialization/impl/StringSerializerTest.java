package jdk.aprismate.serialization.impl;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class StringSerializerTest {
    
    private final StringSerializer serializer = new StringSerializer();
    
    @Test
    void testGetType() {
        assertEquals(String.class, serializer.getType());
    }
    
    @Test
    void testGetFormat() {
        assertEquals("utf8", serializer.getFormat());
    }
    
    @Test
    void testSerializeDeserialize() throws Exception {
        String original = "Hello, AprismJDK!";
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(original, baos);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        String deserialized = serializer.deserialize(bais);
        
        assertEquals(original, deserialized);
    }
    
    @Test
    void testSerializeEmptyString() throws Exception {
        String original = "";
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(original, baos);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        String deserialized = serializer.deserialize(bais);
        
        assertEquals(original, deserialized);
    }
    
    @Test
    void testSerializeUnicode() throws Exception {
        String original = "Hello 世界 🌍";
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(original, baos);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        String deserialized = serializer.deserialize(bais);
        
        assertEquals(original, deserialized);
    }
    
    @Test
    void testSerializeLongString() throws Exception {
        String original = "a".repeat(10000);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(original, baos);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        String deserialized = serializer.deserialize(bais);
        
        assertEquals(original, deserialized);
    }
    
    @Test
    void testDeserializeInvalidLength() {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[]{-1, -1, -1, -1});
        assertThrows(Exception.class, () -> serializer.deserialize(bais));
    }
    
    @Test
    void testDeserializeTooLarge() {
        byte[] data = new byte[8];
        data[0] = 0x7F; // Large positive int
        data[1] = (byte) 0xFF;
        data[2] = (byte) 0xFF;
        data[3] = (byte) 0xFF;
        
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        assertThrows(Exception.class, () -> serializer.deserialize(bais));
    }
}
