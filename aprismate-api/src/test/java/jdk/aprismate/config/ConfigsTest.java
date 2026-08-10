package jdk.aprismate.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Tests for Configs static API.
 */
class ConfigsTest {
    
    @AfterEach
    void cleanup() {
        Configs.clear();
    }
    
    @Test
    void isInitializedShouldReturnFalseInitially() {
        assertFalse(Configs.isInitialized());
    }
    
    @Test
    void setManagerShouldInitialize() {
        ConfigManager manager = new TestConfigManager();
        Configs.setManager(manager);
        assertTrue(Configs.isInitialized());
    }
    
    @Test
    void setManagerShouldRejectNull() {
        assertThrows(NullPointerException.class, () -> {
            Configs.setManager(null);
        });
    }
    
    @Test
    void setManagerShouldRejectDuplicateInitialization() {
        Configs.setManager(new TestConfigManager());
        assertThrows(IllegalStateException.class, () -> {
            Configs.setManager(new TestConfigManager());
        });
    }
    
    @Test
    void getManagerShouldReturnSetManager() {
        ConfigManager manager = new TestConfigManager();
        Configs.setManager(manager);
        assertSame(manager, Configs.getManager());
    }
    
    @Test
    void getManagerShouldThrowIfNotInitialized() {
        assertThrows(IllegalStateException.class, () -> {
            Configs.getManager();
        });
    }
    
    @Test
    void clearShouldResetState() {
        Configs.setManager(new TestConfigManager());
        assertTrue(Configs.isInitialized());
        
        Configs.clear();
        assertFalse(Configs.isInitialized());
    }
    
    @Test
    void cannotInstantiateConfigs() throws Exception {
        java.lang.reflect.Constructor<?> constructor = 
            Configs.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        
        java.lang.reflect.InvocationTargetException exception = 
            assertThrows(java.lang.reflect.InvocationTargetException.class, () -> {
                constructor.newInstance();
            });
        
        assertTrue(exception.getCause() instanceof UnsupportedOperationException);
    }
    
    // Test implementation of ConfigManager
    private static class TestConfigManager implements ConfigManager {
        @Override
        public Config load(Path path) throws IOException, ConfigException {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public Config load(Path path, String format) throws IOException, ConfigException {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void save(Config config, Path path) throws IOException, ConfigException {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void save(Config config, Path path, String format) throws IOException, ConfigException {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public Config getModConfig(String modId, String fileName) throws IOException, ConfigException {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public Config getSystemConfig() throws IOException, ConfigException {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void registerLoader(ConfigLoader loader) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public Optional<ConfigLoader> getLoader(String format) {
            return Optional.empty();
        }
        
        @Override
        public Optional<ConfigLoader> getLoaderForFile(Path path) {
            return Optional.empty();
        }
        
        @Override
        public void reloadAll() throws IOException, ConfigException {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void saveAll() throws IOException, ConfigException {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public Path getConfigDirectory() {
            return Path.of("config");
        }
    }
}
