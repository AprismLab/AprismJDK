package aprism.agent.optimize;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Minimal classloader for executing transformed fixture bytecode in tests. */
final class ByteArrayClassLoader extends ClassLoader {

    private final Map<String, byte[]> defs = new ConcurrentHashMap<>();

    Class<?> define(String name, byte[] bytes) {
        defs.put(name, bytes);
        Class<?> c;
        try {
            c = loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
        resolve(c);
        return c;
    }

    private static void resolve(Class<?> c) {
        if (!c.isAssignableFrom(Object.class)) {
            // no-op; linkage happens on first use
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] b = defs.get(name);
        if (b == null) {
            throw new ClassNotFoundException(name);
        }
        return defineClass(name, b, 0, b.length);
    }
}
