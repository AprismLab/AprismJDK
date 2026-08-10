package jdk.aprismate.mod;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ModRegistry static API.
 */
class ModRegistryTest {
    
    @Test
    void getModShouldRejectNullModId() {
        assertThrows(NullPointerException.class, () -> {
            ModRegistry.getMod(null);
        }, "getMod() should reject null modId");
    }
    
    @Test
    void isModLoadedShouldRejectNullModId() {
        assertThrows(NullPointerException.class, () -> {
            ModRegistry.isModLoaded(null);
        }, "isModLoaded() should reject null modId");
    }
    
    @Test
    void getModFromClassShouldRejectNullClass() {
        assertThrows(NullPointerException.class, () -> {
            ModRegistry.getModFromClass(null);
        }, "getModFromClass() should reject null class");
    }
    
    @Test
    void getAllModsShouldThrowUnsupportedOperation() {
        assertThrows(UnsupportedOperationException.class, () -> {
            ModRegistry.getAllMods();
        }, "getAllMods() not yet implemented");
    }
    
    @Test
    void getModShouldThrowUnsupportedOperation() {
        assertThrows(UnsupportedOperationException.class, () -> {
            ModRegistry.getMod("test-mod");
        }, "getMod() not yet implemented");
    }
    
    @Test
    void getModFromClassShouldThrowUnsupportedOperation() {
        assertThrows(UnsupportedOperationException.class, () -> {
            ModRegistry.getModFromClass(String.class);
        }, "getModFromClass() not yet implemented");
    }
    
    @Test
    void cannotInstantiateModRegistry() throws Exception {
        java.lang.reflect.Constructor<?> constructor = 
            ModRegistry.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        
        java.lang.reflect.InvocationTargetException exception = 
            assertThrows(java.lang.reflect.InvocationTargetException.class, () -> {
                constructor.newInstance();
            }, "ModRegistry should not be instantiable");
        
        assertTrue(exception.getCause() instanceof UnsupportedOperationException,
            "Cause should be UnsupportedOperationException");
        assertEquals("Cannot instantiate ModRegistry", exception.getCause().getMessage());
    }
}
