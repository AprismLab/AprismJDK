package jdk.aprismate.jit;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Method;

/**
 * Tests for CompileImmediately annotation.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
class CompileImmediatelyTest {
    
    @CompileImmediately
    public void annotatedMethod() {
        // Test method
    }
    
    public void unannotatedMethod() {
        // Test method
    }
    
    @Test
    void testAnnotationPresent() throws Exception {
        Method annotated = getClass().getMethod("annotatedMethod");
        assertTrue(annotated.isAnnotationPresent(CompileImmediately.class));
        
        Method unannotated = getClass().getMethod("unannotatedMethod");
        assertFalse(unannotated.isAnnotationPresent(CompileImmediately.class));
    }
    
    @Test
    void testAnnotationRetention() throws Exception {
        Method method = getClass().getMethod("annotatedMethod");
        CompileImmediately annotation = method.getAnnotation(CompileImmediately.class);
        assertNotNull(annotation);
    }
}
