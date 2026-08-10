package aprism.agent.transform;

import jdk.aprismate.agent.BytecodeTransformer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default implementation of BytecodeTransformer.
 * Thread-safe implementation using CopyOnWriteArrayList for transformer storage.
 */
public class DefaultBytecodeTransformer implements BytecodeTransformer {
    
    private static final Logger LOGGER = Logger.getLogger(DefaultBytecodeTransformer.class.getName());
    
    private final Instrumentation instrumentation;
    private final CopyOnWriteArrayList<TransformerEntry> transformers;
    
    public DefaultBytecodeTransformer(Instrumentation instrumentation) {
        this.instrumentation = Objects.requireNonNull(instrumentation, "instrumentation cannot be null");
        this.transformers = new CopyOnWriteArrayList<>();
    }
    
    @Override
    public void registerTransformer(ClassFileTransformer transformer) {
        registerTransformer(transformer, false);
    }
    
    @Override
    public void registerTransformer(ClassFileTransformer transformer, boolean canRetransform) {
        Objects.requireNonNull(transformer, "transformer cannot be null");
        
        TransformerEntry entry = new TransformerEntry(transformer, canRetransform);
        transformers.add(entry);
        instrumentation.addTransformer(transformer, canRetransform);
        
        LOGGER.log(Level.INFO, "Registered transformer: {0} (canRetransform={1})", 
                   new Object[]{transformer.getClass().getName(), canRetransform});
    }
    
    @Override
    public boolean unregisterTransformer(ClassFileTransformer transformer) {
        Objects.requireNonNull(transformer, "transformer cannot be null");
        
        boolean removed = transformers.removeIf(entry -> entry.transformer == transformer);
        if (removed) {
            boolean instrumentationRemoved = instrumentation.removeTransformer(transformer);
            LOGGER.log(Level.INFO, "Unregistered transformer: {0} (success={1})", 
                       new Object[]{transformer.getClass().getName(), instrumentationRemoved});
            return instrumentationRemoved;
        }
        return false;
    }
    
    @Override
    public List<ClassFileTransformer> getTransformers() {
        List<ClassFileTransformer> result = new ArrayList<>(transformers.size());
        for (TransformerEntry entry : transformers) {
            result.add(entry.transformer);
        }
        return Collections.unmodifiableList(result);
    }
    
    @Override
    public int getTransformerCount() {
        return transformers.size();
    }
    
    @Override
    public void retransformClasses(Class<?>... classes) {
        if (classes == null || classes.length == 0) {
            throw new IllegalArgumentException("classes cannot be null or empty");
        }
        
        if (!isRetransformSupported()) {
            throw new UnsupportedOperationException("Retransformation is not supported");
        }
        
        try {
            instrumentation.retransformClasses(classes);
            LOGGER.log(Level.INFO, "Retransformed {0} classes", classes.length);
        } catch (UnmodifiableClassException e) {
            LOGGER.log(Level.SEVERE, "Failed to retransform classes", e);
            throw new RuntimeException("Retransformation failed", e);
        }
    }
    
    @Override
    public boolean isRetransformSupported() {
        return instrumentation.isRetransformClassesSupported();
    }
    
    @Override
    public void clearTransformers() {
        for (TransformerEntry entry : transformers) {
            instrumentation.removeTransformer(entry.transformer);
        }
        transformers.clear();
        LOGGER.log(Level.INFO, "Cleared all transformers");
    }
    
    /**
     * Internal entry holding transformer and its retransform capability.
     */
    private static class TransformerEntry {
        final ClassFileTransformer transformer;
        final boolean canRetransform;
        
        TransformerEntry(ClassFileTransformer transformer, boolean canRetransform) {
            this.transformer = transformer;
            this.canRetransform = canRetransform;
        }
    }
}
