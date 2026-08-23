package jdk.aprismate.agent;

import java.lang.instrument.ClassFileTransformer;
import java.util.List;

/**
 * Manages bytecode transformers for load-time class transformation.
 * Provides a simplified API for registering and managing ClassFileTransformers
 * with support for ordering and conditional transformation.
 *
 * @since AprismJDK 26.1-Alpha.5
 */
public interface BytecodeTransformer {
    
    /**
     * Registers a class file transformer for load-time bytecode transformation.
     * Transformers are invoked in registration order during class loading.
     *
     * @param transformer the transformer to register
     * @throws IllegalArgumentException if transformer is null
     */
    void registerTransformer(ClassFileTransformer transformer);
    
    /**
     * Registers a class file transformer with retransformation capability.
     * When canRetransform is true, the transformer can be applied to already loaded classes.
     *
     * @param transformer the transformer to register
     * @param canRetransform whether this transformer can retransform classes
     * @throws IllegalArgumentException if transformer is null
     */
    void registerTransformer(ClassFileTransformer transformer, boolean canRetransform);
    
    /**
     * Unregisters a previously registered transformer.
     * After unregistration, the transformer will no longer be invoked for new class loads.
     *
     * @param transformer the transformer to unregister
     * @return true if the transformer was found and removed, false otherwise
     */
    boolean unregisterTransformer(ClassFileTransformer transformer);
    
    /**
     * Gets all currently registered transformers in registration order.
     *
     * @return immutable list of registered transformers
     */
    List<ClassFileTransformer> getTransformers();
    
    /**
     * Gets the number of currently registered transformers.
     *
     * @return transformer count
     */
    int getTransformerCount();
    
    /**
     * Retransforms the specified classes using all retransform-capable transformers.
     * This allows modification of already loaded classes.
     *
     * @param classes the classes to retransform
     * @throws IllegalArgumentException if classes array is null or empty
     * @throws UnsupportedOperationException if retransformation is not supported
     */
    void retransformClasses(Class<?>... classes);
    
    /**
     * Checks if retransformation is supported by the current JVM.
     *
     * @return true if retransformation is supported
     */
    boolean isRetransformSupported();
    
    /**
     * Clears all registered transformers.
     * This is primarily useful for testing or cleanup scenarios.
     */
    void clearTransformers();
}
