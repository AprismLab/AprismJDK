package jdk.aprismate.resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Tests for Resources static API.
 */
class ResourcesTest {
    
    @AfterEach
    void cleanup() {
        Resources.clear();
    }
    
    @Test
    void isInitializedShouldReturnFalseInitially() {
        assertFalse(Resources.isInitialized());
    }
    
    @Test
    void setManagerShouldInitialize() {
        ResourceManager manager = new TestResourceManager();
        Resources.setManager(manager);
        assertTrue(Resources.isInitialized());
    }
    
    @Test
    void setManagerShouldRejectNull() {
        assertThrows(NullPointerException.class, () -> {
            Resources.setManager(null);
        });
    }
    
    @Test
    void setManagerShouldRejectDuplicateInitialization() {
        Resources.setManager(new TestResourceManager());
        assertThrows(IllegalStateException.class, () -> {
            Resources.setManager(new TestResourceManager());
        });
    }
    
    @Test
    void getManagerShouldReturnSetManager() {
        ResourceManager manager = new TestResourceManager();
        Resources.setManager(manager);
        assertSame(manager, Resources.getManager());
    }
    
    @Test
    void getManagerShouldThrowIfNotInitialized() {
        assertThrows(IllegalStateException.class, () -> {
            Resources.getManager();
        });
    }
    
    @Test
    void clearShouldResetState() {
        Resources.setManager(new TestResourceManager());
        assertTrue(Resources.isInitialized());
        
        Resources.clear();
        assertFalse(Resources.isInitialized());
    }
    
    @Test
    void cannotInstantiateResources() throws Exception {
        java.lang.reflect.Constructor<?> constructor = 
            Resources.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        
        java.lang.reflect.InvocationTargetException exception = 
            assertThrows(java.lang.reflect.InvocationTargetException.class, () -> {
                constructor.newInstance();
            });
        
        assertTrue(exception.getCause() instanceof UnsupportedOperationException);
    }
    
    // Test implementation of ResourceManager
    private static class TestResourceManager implements ResourceManager {
        @Override
        public Optional<Resource> getResource(ResourceLocation location) {
            return Optional.empty();
        }
        
        @Override
        public Collection<Resource> getAllResources(ResourceLocation location) {
            return Collections.emptyList();
        }
        
        @Override
        public Collection<Resource> findResources(Predicate<ResourceLocation> filter) {
            return Collections.emptyList();
        }
        
        @Override
        public Collection<Resource> findResourcesInNamespace(String namespace) {
            return Collections.emptyList();
        }
        
        @Override
        public Collection<Resource> findResourcesByPath(String pathPrefix) {
            return Collections.emptyList();
        }
        
        @Override
        public void reload() throws IOException {
        }
        
        @Override
        public Collection<String> getNamespaces() {
            return Collections.emptyList();
        }
    }
}
