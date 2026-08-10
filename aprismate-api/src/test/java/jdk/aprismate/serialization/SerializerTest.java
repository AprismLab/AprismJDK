package jdk.aprismate.serialization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SerializerTest {
    
    private TestSerializer serializer;
    
    @BeforeEach
    void setUp() {
        serializer = new TestSerializer();
    }
    
    @Test
    void testGetType() {
        assertEquals(TestObject.class, serializer.getType());
    }
    
    @Test
    void testGetFormat() {
        assertEquals("binary", serializer.getFormat());
    }
    
    @Test
    void testSupports() {
        assertTrue(serializer.supports(TestObject.class));
        assertFalse(serializer.supports(String.class));
    }
    
    @Test
    void testSerializeDeserialize() throws IOException, SerializationException {
        TestObject obj = new TestObject("test", 42);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(obj, baos);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        TestObject deserialized = serializer.deserialize(bais);
        
        assertEquals(obj.name, deserialized.name);
        assertEquals(obj.value, deserialized.value);
    }
    
    @Test
    void testSerializeNull() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertThrows(NullPointerException.class, () -> serializer.serialize(null, baos));
    }
    
    @Test
    void testSerializeNullOutput() {
        TestObject obj = new TestObject("test", 42);
        assertThrows(NullPointerException.class, () -> serializer.serialize(obj, null));
    }
    
    @Test
    void testDeserializeNull() {
        assertThrows(NullPointerException.class, () -> serializer.deserialize(null));
    }
    
    // Test helpers
    
    static class TestObject {
        String name;
        int value;
        
        TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }
    
    static class TestSerializer implements Serializer<TestObject> {
        
        @Override
        public void serialize(TestObject object, java.io.OutputStream output) throws IOException {
            if (object == null) throw new NullPointerException("object cannot be null");
            if (output == null) throw new NullPointerException("output cannot be null");
            
            java.io.DataOutputStream dos = new java.io.DataOutputStream(output);
            byte[] nameBytes = object.name.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            dos.writeInt(nameBytes.length);
            dos.write(nameBytes);
            dos.writeInt(object.value);
            dos.flush();
        }
        
        @Override
        public TestObject deserialize(java.io.InputStream input) throws IOException {
            if (input == null) throw new NullPointerException("input cannot be null");
            
            java.io.DataInputStream dis = new java.io.DataInputStream(input);
            int nameLength = dis.readInt();
            byte[] nameBytes = new byte[nameLength];
            dis.readFully(nameBytes);
            String name = new String(nameBytes, java.nio.charset.StandardCharsets.UTF_8);
            int value = dis.readInt();
            return new TestObject(name, value);
        }
        
        @Override
        public Class<TestObject> getType() {
            return TestObject.class;
        }
    }
}
