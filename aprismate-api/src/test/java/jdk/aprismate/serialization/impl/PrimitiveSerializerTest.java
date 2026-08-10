package jdk.aprismate.serialization.impl;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class PrimitiveSerializerTest {
    
    @Test
    void testIntegerSerializer() throws Exception {
        PrimitiveSerializer<Integer> serializer = new PrimitiveSerializer<>(Integer.class);
        
        assertEquals(Integer.class, serializer.getType());
        
        Integer original = 42;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(original, baos);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Integer deserialized = serializer.deserialize(bais);
        
        assertEquals(original, deserialized);
    }
    
    @Test
    void testLongSerializer() throws Exception {
        PrimitiveSerializer<Long> serializer = new PrimitiveSerializer<>(Long.class);
        
        Long original = 9876543210L;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(original, baos);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Long deserialized = serializer.deserialize(bais);
        
        assertEquals(original, deserialized);
    }
    
    @Test
    void testDoubleSerializer() throws Exception {
        PrimitiveSerializer<Double> serializer = new PrimitiveSerializer<>(Double.class);
        
        Double original = 3.14159;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(original, baos);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Double deserialized = serializer.deserialize(bais);
        
        assertEquals(original, deserialized);
    }
    
    @Test
    void testFloatSerializer() throws Exception {
        PrimitiveSerializer<Float> serializer = new PrimitiveSerializer<>(Float.class);
        
        Float original = 2.718f;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(original, baos);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Float deserialized = serializer.deserialize(bais);
        
        assertEquals(original, deserialized);
    }
    
    @Test
    void testBooleanSerializer() throws Exception {
        PrimitiveSerializer<Boolean> serializer = new PrimitiveSerializer<>(Boolean.class);
        
        Boolean original = true;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(original, baos);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Boolean deserialized = serializer.deserialize(bais);
        
        assertEquals(original, deserialized);
    }
    
    @Test
    void testByteSerializer() throws Exception {
        PrimitiveSerializer<Byte> serializer = new PrimitiveSerializer<>(Byte.class);
        
        Byte original = (byte) 127;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(original, baos);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Byte deserialized = serializer.deserialize(bais);
        
        assertEquals(original, deserialized);
    }
    
    @Test
    void testShortSerializer() throws Exception {
        PrimitiveSerializer<Short> serializer = new PrimitiveSerializer<>(Short.class);
        
        Short original = (short) 32000;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(original, baos);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Short deserialized = serializer.deserialize(bais);
        
        assertEquals(original, deserialized);
    }
    
    @Test
    void testCharacterSerializer() throws Exception {
        PrimitiveSerializer<Character> serializer = new PrimitiveSerializer<>(Character.class);
        
        Character original = 'A';
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(original, baos);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Character deserialized = serializer.deserialize(bais);
        
        assertEquals(original, deserialized);
    }
    
    @Test
    void testInvalidType() {
        assertThrows(IllegalArgumentException.class, 
            () -> new PrimitiveSerializer<>(String.class));
    }
    
    @Test
    void testGetFormat() {
        PrimitiveSerializer<Integer> serializer = new PrimitiveSerializer<>(Integer.class);
        assertEquals("binary", serializer.getFormat());
    }
}
