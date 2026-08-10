package jdk.aprismate.resource;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ResourceLocation class.
 */
class ResourceLocationTest {
    
    @Test
    void constructorShouldCreateValidLocation() {
        ResourceLocation loc = new ResourceLocation("minecraft", "stone");
        assertEquals("minecraft", loc.getNamespace());
        assertEquals("stone", loc.getPath());
    }
    
    @Test
    void constructorShouldRejectNullNamespace() {
        assertThrows(NullPointerException.class, () -> {
            new ResourceLocation(null, "stone");
        });
    }
    
    @Test
    void constructorShouldRejectNullPath() {
        assertThrows(NullPointerException.class, () -> {
            new ResourceLocation("minecraft", null);
        });
    }
    
    @Test
    void constructorShouldRejectInvalidNamespace() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ResourceLocation("Invalid_Namespace", "stone");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new ResourceLocation("name space", "stone");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new ResourceLocation("name/space", "stone");
        });
    }
    
    @Test
    void constructorShouldRejectInvalidPath() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ResourceLocation("minecraft", "Invalid Path");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new ResourceLocation("minecraft", "path:colon");
        });
    }
    
    @Test
    void constructorShouldAcceptValidCharacters() {
        // Valid namespace characters: a-z 0-9 _ -
        assertDoesNotThrow(() -> new ResourceLocation("my-mod_123", "item"));
        
        // Valid path characters: a-z 0-9 _ - . /
        assertDoesNotThrow(() -> new ResourceLocation("mod", "items/tools/sword.json"));
        assertDoesNotThrow(() -> new ResourceLocation("mod", "data-file_v2.0"));
    }
    
    @Test
    void ofShouldParseLocationWithColon() {
        ResourceLocation loc = ResourceLocation.of("minecraft:stone");
        assertEquals("minecraft", loc.getNamespace());
        assertEquals("stone", loc.getPath());
    }
    
    @Test
    void ofShouldUseDefaultNamespaceWithoutColon() {
        ResourceLocation loc = ResourceLocation.of("items/sword");
        assertEquals("aprism", loc.getNamespace());
        assertEquals("items/sword", loc.getPath());
    }
    
    @Test
    void ofShouldRejectNull() {
        assertThrows(NullPointerException.class, () -> {
            ResourceLocation.of(null);
        });
    }
    
    @Test
    void ofShouldRejectInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> {
            ResourceLocation.of("Invalid:Path");
        });
    }
    
    @Test
    void getDefaultNamespaceShouldReturnAprism() {
        assertEquals("aprism", ResourceLocation.getDefaultNamespace());
    }
    
    @Test
    void isDefaultNamespaceShouldReturnTrueForDefault() {
        ResourceLocation loc = new ResourceLocation("aprism", "test");
        assertTrue(loc.isDefaultNamespace());
    }
    
    @Test
    void isDefaultNamespaceShouldReturnFalseForNonDefault() {
        ResourceLocation loc = new ResourceLocation("minecraft", "test");
        assertFalse(loc.isDefaultNamespace());
    }
    
    @Test
    void toStringShouldFormatCorrectly() {
        ResourceLocation loc = new ResourceLocation("minecraft", "stone");
        assertEquals("minecraft:stone", loc.toString());
    }
    
    @Test
    void equalsShouldCompareNamespaceAndPath() {
        ResourceLocation loc1 = new ResourceLocation("minecraft", "stone");
        ResourceLocation loc2 = new ResourceLocation("minecraft", "stone");
        ResourceLocation loc3 = new ResourceLocation("minecraft", "dirt");
        ResourceLocation loc4 = new ResourceLocation("mod", "stone");
        
        assertEquals(loc1, loc2);
        assertNotEquals(loc1, loc3);
        assertNotEquals(loc1, loc4);
    }
    
    @Test
    void equalsShouldHandleSameInstance() {
        ResourceLocation loc = new ResourceLocation("minecraft", "stone");
        assertEquals(loc, loc);
    }
    
    @Test
    void equalsShouldHandleNull() {
        ResourceLocation loc = new ResourceLocation("minecraft", "stone");
        assertNotEquals(loc, null);
    }
    
    @Test
    void equalsShouldHandleDifferentType() {
        ResourceLocation loc = new ResourceLocation("minecraft", "stone");
        assertNotEquals(loc, "minecraft:stone");
    }
    
    @Test
    void hashCodeShouldBeConsistent() {
        ResourceLocation loc1 = new ResourceLocation("minecraft", "stone");
        ResourceLocation loc2 = new ResourceLocation("minecraft", "stone");
        assertEquals(loc1.hashCode(), loc2.hashCode());
    }
    
    @Test
    void compareToShouldOrderByNamespaceThenPath() {
        ResourceLocation a = new ResourceLocation("aaa", "zzz");
        ResourceLocation b = new ResourceLocation("bbb", "aaa");
        ResourceLocation c = new ResourceLocation("bbb", "zzz");
        
        assertTrue(a.compareTo(b) < 0); // namespace comes first
        assertTrue(b.compareTo(c) < 0); // then path
        assertTrue(a.compareTo(c) < 0);
    }
    
    @Test
    void compareToShouldReturnZeroForEqual() {
        ResourceLocation loc1 = new ResourceLocation("minecraft", "stone");
        ResourceLocation loc2 = new ResourceLocation("minecraft", "stone");
        assertEquals(0, loc1.compareTo(loc2));
    }
}
