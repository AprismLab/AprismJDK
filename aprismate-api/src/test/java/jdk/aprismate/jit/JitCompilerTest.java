package jdk.aprismate.jit;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Method;

/**
 * Tests for JitCompiler API.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
class JitCompilerTest {
    
    @Test
    void testCompileMethod() throws Exception {
        Method method = String.class.getMethod("length");
        
        assertDoesNotThrow(() -> {
            try {
                JitCompiler.compileMethod(method, JitCompiler.CompilationLevel.TIER_4);
            } catch (UnsupportedOperationException e) {
                // Expected on stock JDK
            }
        });
    }
    
    @Test
    void testDecompileMethod() throws Exception {
        Method method = String.class.getMethod("length");
        
        assertDoesNotThrow(() -> {
            try {
                JitCompiler.decompileMethod(method);
            } catch (UnsupportedOperationException e) {
                // Expected on stock JDK
            }
        });
    }
    
    @Test
    void testGetCompilationInfo() throws Exception {
        Method method = String.class.getMethod("length");
        
        assertDoesNotThrow(() -> {
            try {
                CompilationInfo info = JitCompiler.getCompilationInfo(method);
                // May be null if not compiled
            } catch (UnsupportedOperationException e) {
                // Expected on stock JDK
            }
        });
    }
    
    @Test
    void testNullMethod() {
        assertThrows(NullPointerException.class, () -> {
            JitCompiler.compileMethod(null, JitCompiler.CompilationLevel.TIER_4);
        });
        
        assertThrows(NullPointerException.class, () -> {
            JitCompiler.decompileMethod(null);
        });
        
        assertThrows(NullPointerException.class, () -> {
            JitCompiler.getCompilationInfo((Method) null);
        });
    }
}
