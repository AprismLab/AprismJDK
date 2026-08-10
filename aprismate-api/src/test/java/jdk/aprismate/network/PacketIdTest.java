package jdk.aprismate.network;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PacketId class.
 */
class PacketIdTest {
    
    @Test
    void constructorShouldCreateValidPacketId() {
        PacketId id = new PacketId("aprism", "handshake");
        assertEquals("aprism", id.getNamespace());
        assertEquals("handshake", id.getPath());
    }
    
    @Test
    void constructorShouldRejectNullNamespace() {
        assertThrows(NullPointerException.class, () -> {
            new PacketId(null, "path");
        });
    }
    
    @Test
    void constructorShouldRejectNullPath() {
        assertThrows(NullPointerException.class, () -> {
            new PacketId("namespace", null);
        });
    }
    
    @Test
    void constructorShouldRejectInvalidNamespace() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PacketId("Invalid", "path");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new PacketId("name space", "path");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new PacketId("name/space", "path");
        });
    }
    
    @Test
    void constructorShouldRejectInvalidPath() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PacketId("namespace", "Invalid");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new PacketId("namespace", "pa th");
        });
    }
    
    @Test
    void constructorShouldAcceptValidCharacters() {
        assertDoesNotThrow(() -> {
            new PacketId("my-namespace", "my_path.test");
        });
    }
    
    @Test
    void ofShouldParsePacketIdWithColon() {
        PacketId id = PacketId.of("aprism:handshake");
        assertEquals("aprism", id.getNamespace());
        assertEquals("handshake", id.getPath());
    }
    
    @Test
    void ofShouldUseDefaultNamespaceWithoutColon() {
        PacketId id = PacketId.of("handshake");
        assertEquals("aprism", id.getNamespace());
        assertEquals("handshake", id.getPath());
    }
    
    @Test
    void ofShouldRejectNull() {
        assertThrows(NullPointerException.class, () -> {
            PacketId.of(null);
        });
    }
    
    @Test
    void ofShouldRejectInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> {
            PacketId.of(":path");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            PacketId.of("namespace:");
        });
    }
    
    @Test
    void getDefaultNamespaceShouldReturnAprism() {
        assertEquals("aprism", PacketId.getDefaultNamespace());
    }
    
    @Test
    void isDefaultNamespaceShouldReturnTrueForDefault() {
        PacketId id = PacketId.of("handshake");
        assertTrue(id.isDefaultNamespace());
    }
    
    @Test
    void isDefaultNamespaceShouldReturnFalseForNonDefault() {
        PacketId id = new PacketId("mymod", "packet");
        assertFalse(id.isDefaultNamespace());
    }
    
    @Test
    void toStringShouldFormatCorrectly() {
        PacketId id = new PacketId("aprism", "handshake");
        assertEquals("aprism:handshake", id.toString());
    }
    
    @Test
    void equalsShouldHandleSameInstance() {
        PacketId id = PacketId.of("aprism:handshake");
        assertEquals(id, id);
    }
    
    @Test
    void equalsShouldCompareNamespaceAndPath() {
        PacketId id1 = new PacketId("aprism", "handshake");
        PacketId id2 = new PacketId("aprism", "handshake");
        PacketId id3 = new PacketId("mymod", "handshake");
        PacketId id4 = new PacketId("aprism", "chat");
        
        assertEquals(id1, id2);
        assertNotEquals(id1, id3);
        assertNotEquals(id1, id4);
    }
    
    @Test
    void equalsShouldHandleNull() {
        PacketId id = PacketId.of("aprism:handshake");
        assertNotEquals(id, null);
    }
    
    @Test
    void equalsShouldHandleDifferentType() {
        PacketId id = PacketId.of("aprism:handshake");
        assertNotEquals(id, "aprism:handshake");
    }
    
    @Test
    void hashCodeShouldBeConsistent() {
        PacketId id1 = new PacketId("aprism", "handshake");
        PacketId id2 = new PacketId("aprism", "handshake");
        
        assertEquals(id1.hashCode(), id2.hashCode());
    }
    
    @Test
    void compareToShouldOrderByNamespaceThenPath() {
        PacketId id1 = new PacketId("aprism", "aaa");
        PacketId id2 = new PacketId("aprism", "bbb");
        PacketId id3 = new PacketId("mymod", "aaa");
        
        assertTrue(id1.compareTo(id2) < 0);
        assertTrue(id2.compareTo(id1) > 0);
        assertTrue(id1.compareTo(id3) < 0);
        assertTrue(id3.compareTo(id1) > 0);
    }
    
    @Test
    void compareToShouldReturnZeroForEqual() {
        PacketId id1 = new PacketId("aprism", "handshake");
        PacketId id2 = new PacketId("aprism", "handshake");
        
        assertEquals(0, id1.compareTo(id2));
    }
}
