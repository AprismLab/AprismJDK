package jdk.aprismate.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ConfigException class.
 */
class ConfigExceptionTest {
    
    @Test
    void constructorWithMessageShouldSetMessage() {
        ConfigException ex = new ConfigException("test message");
        assertEquals("test message", ex.getMessage());
        assertNull(ex.getCause());
    }
    
    @Test
    void constructorWithMessageAndCauseShouldSetBoth() {
        Throwable cause = new RuntimeException("cause");
        ConfigException ex = new ConfigException("test message", cause);
        
        assertEquals("test message", ex.getMessage());
        assertSame(cause, ex.getCause());
    }
    
    @Test
    void constructorWithCauseShouldSetCause() {
        Throwable cause = new RuntimeException("cause");
        ConfigException ex = new ConfigException(cause);
        
        assertSame(cause, ex.getCause());
    }
    
    @Test
    void shouldBeThrowable() {
        ConfigException ex = new ConfigException("test");
        assertThrows(ConfigException.class, () -> {
            throw ex;
        });
    }
    
    @Test
    void shouldBeException() {
        ConfigException ex = new ConfigException("test");
        assertTrue(ex instanceof Exception);
    }
}
