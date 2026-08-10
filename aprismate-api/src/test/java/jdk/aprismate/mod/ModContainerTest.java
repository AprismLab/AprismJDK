package jdk.aprismate.mod;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ModContainer interface.
 */
class ModContainerTest {
    
    @Test
    void modStateShouldHaveFiveStates() {
        ModContainer.ModState[] states = ModContainer.ModState.values();
        assertEquals(5, states.length, "Should have exactly 5 mod states");
    }
    
    @Test
    void modStateShouldHaveDiscovered() {
        ModContainer.ModState state = ModContainer.ModState.valueOf("DISCOVERED");
        assertNotNull(state);
        assertEquals(ModContainer.ModState.DISCOVERED, state);
    }
    
    @Test
    void modStateShouldHaveLoading() {
        ModContainer.ModState state = ModContainer.ModState.valueOf("LOADING");
        assertNotNull(state);
        assertEquals(ModContainer.ModState.LOADING, state);
    }
    
    @Test
    void modStateShouldHaveLoaded() {
        ModContainer.ModState state = ModContainer.ModState.valueOf("LOADED");
        assertNotNull(state);
        assertEquals(ModContainer.ModState.LOADED, state);
    }
    
    @Test
    void modStateShouldHaveErrored() {
        ModContainer.ModState state = ModContainer.ModState.valueOf("ERRORED");
        assertNotNull(state);
        assertEquals(ModContainer.ModState.ERRORED, state);
    }
    
    @Test
    void modStateShouldHaveDisabled() {
        ModContainer.ModState state = ModContainer.ModState.valueOf("DISABLED");
        assertNotNull(state);
        assertEquals(ModContainer.ModState.DISABLED, state);
    }
    
    @Test
    void isLoadedShouldReturnTrueWhenLoaded() {
        ModContainer container = new TestModContainer(ModContainer.ModState.LOADED);
        assertTrue(container.isLoaded(), "isLoaded() should return true when state is LOADED");
    }
    
    @Test
    void isLoadedShouldReturnFalseWhenNotLoaded() {
        assertFalse(new TestModContainer(ModContainer.ModState.DISCOVERED).isLoaded());
        assertFalse(new TestModContainer(ModContainer.ModState.LOADING).isLoaded());
        assertFalse(new TestModContainer(ModContainer.ModState.ERRORED).isLoaded());
        assertFalse(new TestModContainer(ModContainer.ModState.DISABLED).isLoaded());
    }
    
    // Test implementation of ModContainer
    private static class TestModContainer implements ModContainer {
        private final ModState state;
        
        TestModContainer(ModState state) {
            this.state = state;
        }
        
        @Override
        public ModMetadata getMetadata() {
            return null;
        }
        
        @Override
        public java.util.Optional<Object> getModInstance() {
            return java.util.Optional.empty();
        }
        
        @Override
        public ModState getState() {
            return state;
        }
        
        @Override
        public java.nio.file.Path getSource() {
            return null;
        }
        
        @Override
        public ClassLoader getClassLoader() {
            return null;
        }
    }
}
