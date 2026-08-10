package jdk.aprismate.mod;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ModEnvironment enum.
 */
class ModEnvironmentTest {
    
    @Test
    void shouldHaveThreeEnvironments() {
        ModEnvironment[] environments = ModEnvironment.values();
        assertEquals(3, environments.length, "Should have exactly 3 environments");
    }
    
    @Test
    void shouldHaveClientEnvironment() {
        ModEnvironment env = ModEnvironment.valueOf("CLIENT");
        assertNotNull(env);
        assertEquals(ModEnvironment.CLIENT, env);
    }
    
    @Test
    void shouldHaveServerEnvironment() {
        ModEnvironment env = ModEnvironment.valueOf("SERVER");
        assertNotNull(env);
        assertEquals(ModEnvironment.SERVER, env);
    }
    
    @Test
    void shouldHaveUniversalEnvironment() {
        ModEnvironment env = ModEnvironment.valueOf("UNIVERSAL");
        assertNotNull(env);
        assertEquals(ModEnvironment.UNIVERSAL, env);
    }
    
    @Test
    void getDefaultShouldReturnUniversal() {
        assertEquals(ModEnvironment.UNIVERSAL, ModEnvironment.getDefault(),
            "Default environment should be UNIVERSAL");
    }
    
    @Test
    void universalShouldBeCompatibleWithAll() {
        assertTrue(ModEnvironment.UNIVERSAL.isCompatibleWith(ModEnvironment.CLIENT));
        assertTrue(ModEnvironment.UNIVERSAL.isCompatibleWith(ModEnvironment.SERVER));
        assertTrue(ModEnvironment.UNIVERSAL.isCompatibleWith(ModEnvironment.UNIVERSAL));
    }
    
    @Test
    void allShouldBeCompatibleWithUniversal() {
        assertTrue(ModEnvironment.CLIENT.isCompatibleWith(ModEnvironment.UNIVERSAL));
        assertTrue(ModEnvironment.SERVER.isCompatibleWith(ModEnvironment.UNIVERSAL));
    }
    
    @Test
    void clientShouldBeCompatibleWithClient() {
        assertTrue(ModEnvironment.CLIENT.isCompatibleWith(ModEnvironment.CLIENT));
    }
    
    @Test
    void serverShouldBeCompatibleWithServer() {
        assertTrue(ModEnvironment.SERVER.isCompatibleWith(ModEnvironment.SERVER));
    }
    
    @Test
    void clientShouldNotBeCompatibleWithServer() {
        assertFalse(ModEnvironment.CLIENT.isCompatibleWith(ModEnvironment.SERVER));
    }
    
    @Test
    void serverShouldNotBeCompatibleWithClient() {
        assertFalse(ModEnvironment.SERVER.isCompatibleWith(ModEnvironment.CLIENT));
    }
}
