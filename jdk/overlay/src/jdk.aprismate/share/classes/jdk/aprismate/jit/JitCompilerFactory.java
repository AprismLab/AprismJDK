package jdk.aprismate.jit;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Factory for JitCompiler operations.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
final class JitCompilerFactory {
    
    private static final Set<DeoptimizationListener> listeners = new HashSet<>();
    
    private JitCompilerFactory() {
        // No instantiation
    }
    
    static boolean compileMethod(Class<?> clazz, String methodName, JitCompiler.CompilationLevel level) {
        Objects.requireNonNull(clazz, "clazz");
        Objects.requireNonNull(methodName, "methodName");
        Objects.requireNonNull(level, "level");
        
        try {
            // Try all methods with this name
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(methodName)) {
                    return compileMethod(method, level);
                }
            }
            throw new IllegalArgumentException("Method not found: " + methodName);
        } catch (Exception e) {
            return false;
        }
    }
    
    static boolean compileMethod(Class<?> clazz, String methodName, 
                                 Class<?>[] parameterTypes, JitCompiler.CompilationLevel level) 
            throws NoSuchMethodException {
        Objects.requireNonNull(clazz, "clazz");
        Objects.requireNonNull(methodName, "methodName");
        Objects.requireNonNull(parameterTypes, "parameterTypes");
        Objects.requireNonNull(level, "level");
        
        Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
        return compileMethod(method, level);
    }
    
    static boolean compileMethod(Method method, JitCompiler.CompilationLevel level) {
        Objects.requireNonNull(method, "method");
        Objects.requireNonNull(level, "level");
        
        try {
            // Try to use agent implementation
            Class<?> implClass = Class.forName("com.aprismate.agent.jit.JitCompilerImpl");
            return (boolean) implClass.getMethod("compileMethod", Method.class, JitCompiler.CompilationLevel.class)
                .invoke(null, method, level);
        } catch (Exception e) {
            // Fallback: not supported on stock JDK
            return false;
        }
    }
    
    static void decompileMethod(Method method) {
        Objects.requireNonNull(method, "method");
        
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.jit.JitCompilerImpl");
            implClass.getMethod("decompileMethod", Method.class).invoke(null, method);
        } catch (Exception e) {
            // Fallback: not supported
        }
    }
    
    static CompilationInfo getCompilationInfo(Class<?> clazz, String methodName) {
        Objects.requireNonNull(clazz, "clazz");
        Objects.requireNonNull(methodName, "methodName");
        
        try {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(methodName)) {
                    return getCompilationInfo(method);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    static CompilationInfo getCompilationInfo(Method method) {
        Objects.requireNonNull(method, "method");
        
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.jit.JitCompilerImpl");
            return (CompilationInfo) implClass.getMethod("getCompilationInfo", Method.class)
                .invoke(null, method);
        } catch (Exception e) {
            return new StubCompilationInfo(method);
        }
    }
    
    @SuppressWarnings("unchecked")
    static Set<Method> getCompiledMethods() {
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.jit.JitCompilerImpl");
            return (Set<Method>) implClass.getMethod("getCompiledMethods").invoke(null);
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }
    
    static void enableProfiling() {
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.jit.JitCompilerImpl");
            implClass.getMethod("enableProfiling").invoke(null);
        } catch (Exception e) {
            // Fallback: not supported
        }
    }
    
    static void disableProfiling() {
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.jit.JitCompilerImpl");
            implClass.getMethod("disableProfiling").invoke(null);
        } catch (Exception e) {
            // Fallback: not supported
        }
    }
    
    static ProfileData collectProfiles() {
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.jit.JitCompilerImpl");
            return (ProfileData) implClass.getMethod("collectProfiles").invoke(null);
        } catch (Exception e) {
            return ProfileDataFactory.empty();
        }
    }
    
    static void applyProfiles(ProfileData profiles) {
        Objects.requireNonNull(profiles, "profiles");
        
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.jit.JitCompilerImpl");
            implClass.getMethod("applyProfiles", ProfileData.class).invoke(null, profiles);
        } catch (Exception e) {
            // Fallback: not supported
        }
    }
    
    static void addDeoptimizationListener(DeoptimizationListener listener) {
        Objects.requireNonNull(listener, "listener");
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    static void removeDeoptimizationListener(DeoptimizationListener listener) {
        Objects.requireNonNull(listener, "listener");
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    static CompilerStats getStats() {
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.jit.JitCompilerImpl");
            return (CompilerStats) implClass.getMethod("getStats").invoke(null);
        } catch (Exception e) {
            return new StubCompilerStats();
        }
    }
    
    static String printAssembly(Method method) {
        Objects.requireNonNull(method, "method");
        
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.jit.JitCompilerImpl");
            return (String) implClass.getMethod("printAssembly", Method.class).invoke(null, method);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Stub CompilationInfo implementation.
     */
    private static class StubCompilationInfo implements CompilationInfo {
        
        private final Method method;
        
        StubCompilationInfo(Method method) {
            this.method = method;
        }
        
        @Override
        public Method method() {
            return method;
        }
        
        @Override
        public boolean isCompiled() {
            return false;
        }
        
        @Override
        public JitCompiler.CompilationLevel level() {
            return null;
        }
        
        @Override
        public long nativeCodeSize() {
            return 0;
        }
        
        @Override
        public long invocationCount() {
            return 0;
        }
        
        @Override
        public long backedgeCount() {
            return 0;
        }
        
        @Override
        public int compilationCount() {
            return 0;
        }
        
        @Override
        public int deoptimizationCount() {
            return 0;
        }
        
        @Override
        public long compilationTime() {
            return 0;
        }
        
        @Override
        public boolean isHot() {
            return false;
        }
        
        @Override
        public boolean isOsrCompiled() {
            return false;
        }
        
        @Override
        public long entryPoint() {
            return 0;
        }
    }
    
    /**
     * Stub CompilerStats implementation.
     */
    private static class StubCompilerStats implements CompilerStats {
        
        @Override
        public long totalCompilations() {
            return 0;
        }
        
        @Override
        public long compilationsAtLevel(JitCompiler.CompilationLevel level) {
            return 0;
        }
        
        @Override
        public long totalDeoptimizations() {
            return 0;
        }
        
        @Override
        public int activeCompiledMethods() {
            return 0;
        }
        
        @Override
        public long totalCompilationTime() {
            return 0;
        }
        
        @Override
        public long totalNativeCodeSize() {
            return 0;
        }
        
        @Override
        public long osrCompilations() {
            return 0;
        }
        
        @Override
        public long standardCompilations() {
            return 0;
        }
        
        @Override
        public long failedCompilations() {
            return 0;
        }
        
        @Override
        public long invalidatedCompilations() {
            return 0;
        }
        
        @Override
        public void reset() {
            // No-op
        }
    }
}
