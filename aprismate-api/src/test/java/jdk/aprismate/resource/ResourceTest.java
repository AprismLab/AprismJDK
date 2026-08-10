package jdk.aprismate.resource;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Tests for Resource interface.
 */
class ResourceTest {
    
    @Test
    void resourceShouldProvideLocation() {
        ResourceLocation loc = ResourceLocation.of("test:resource");
        TestResource resource = new TestResource(loc, "test-mod", "test content");
        
        assertEquals(loc, resource.getLocation());
    }
    
    @Test
    void resourceShouldProvideSource() {
        ResourceLocation loc = ResourceLocation.of("test:resource");
        TestResource resource = new TestResource(loc, "test-mod", "test content");
        
        assertEquals("test-mod", resource.getSource());
    }
    
    @Test
    void openShouldReturnInputStream() throws IOException {
        ResourceLocation loc = ResourceLocation.of("test:resource");
        TestResource resource = new TestResource(loc, "test-mod", "test content");
        
        try (InputStream is = resource.open()) {
            assertNotNull(is);
            byte[] data = is.readAllBytes();
            assertEquals("test content", new String(data));
        }
    }
    
    @Test
    void getSizeShouldReturnEmptyByDefault() {
        TestResource resource = new TestResource(
            ResourceLocation.of("test:resource"), "test-mod", "test content");
        
        assertTrue(resource.getSize().isEmpty());
    }
    
    @Test
    void getSizeShouldReturnSizeWhenKnown() {
        TestResourceWithSize resource = new TestResourceWithSize(
            ResourceLocation.of("test:resource"), "test-mod", "test content", 12L);
        
        assertTrue(resource.getSize().isPresent());
        assertEquals(12L, resource.getSize().get());
    }
    
    @Test
    void existsShouldReturnTrueWhenResourceCanBeOpened() {
        TestResource resource = new TestResource(
            ResourceLocation.of("test:resource"), "test-mod", "test content");
        
        assertTrue(resource.exists());
    }
    
    @Test
    void existsShouldReturnFalseWhenResourceCannotBeOpened() {
        TestResource resource = new TestResource(
            ResourceLocation.of("test:resource"), "test-mod", null);
        
        assertFalse(resource.exists());
    }
    
    @Test
    void closeShouldNotThrowByDefault() {
        TestResource resource = new TestResource(
            ResourceLocation.of("test:resource"), "test-mod", "test content");
        
        assertDoesNotThrow(() -> resource.close());
    }
    
    @Test
    void resourceShouldBeAutoCloseable() {
        TestResource resource = new TestResource(
            ResourceLocation.of("test:resource"), "test-mod", "test content");
        
        assertDoesNotThrow(() -> {
            try (Resource r = resource) {
                // Use resource
            }
        });
    }
    
    // Test implementation of Resource
    private static class TestResource implements Resource {
        private final ResourceLocation location;
        private final String source;
        private final String content;
        
        TestResource(ResourceLocation location, String source, String content) {
            this.location = location;
            this.source = source;
            this.content = content;
        }
        
        @Override
        public ResourceLocation getLocation() {
            return location;
        }
        
        @Override
        public String getSource() {
            return source;
        }
        
        @Override
        public InputStream open() throws IOException {
            if (content == null) {
                throw new IOException("Resource not available");
            }
            return new ByteArrayInputStream(content.getBytes());
        }
    }
    
    // Test implementation with size
    private static class TestResourceWithSize extends TestResource {
        private final long size;
        
        TestResourceWithSize(ResourceLocation location, String source, String content, long size) {
            super(location, source, content);
            this.size = size;
        }
        
        @Override
        public java.util.Optional<Long> getSize() {
            return java.util.Optional.of(size);
        }
    }
}
