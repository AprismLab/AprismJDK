package jdk.aprismate.serialization.impl;

import jdk.aprismate.serialization.Serializer;
import jdk.aprismate.serialization.SerializerRegistry;
import jdk.aprismate.serialization.SerializationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of SerializerRegistry.
 *
 * @since 26.0-Alpha.8
 */
public class SimpleSerializerRegistry implements SerializerRegistry {

    public SimpleSerializerRegistry() {}
    
    private final Map<Class<?>, Serializer<?>> serializers = new ConcurrentHashMap<>();
    
    @Override
    public <T> void registerSerializer(Serializer<T> serializer) {
        Objects.requireNonNull(serializer, "serializer cannot be null");
        serializers.put(serializer.getType(), serializer);
    }
    
    @Override
    public <T> void registerSerializer(Class<T> type, Serializer<T> serializer) {
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(serializer, "serializer cannot be null");
        serializers.put(type, serializer);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<Serializer<T>> getSerializer(Class<T> type) {
        Objects.requireNonNull(type, "type cannot be null");
        
        // Exact match
        Serializer<?> serializer = serializers.get(type);
        if (serializer != null) {
            return Optional.of((Serializer<T>) serializer);
        }
        
        // Find compatible serializer
        for (Map.Entry<Class<?>, Serializer<?>> entry : serializers.entrySet()) {
            if (entry.getValue().supports(type)) {
                return Optional.of((Serializer<T>) entry.getValue());
            }
        }
        
        return Optional.empty();
    }
    
    @Override
    public boolean hasSerializer(Class<?> type) {
        Objects.requireNonNull(type, "type cannot be null");
        return getSerializer(type).isPresent();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void serialize(Object object, OutputStream output) throws IOException, SerializationException {
        Objects.requireNonNull(object, "object cannot be null");
        Objects.requireNonNull(output, "output cannot be null");
        
        Class<?> type = object.getClass();
        Serializer<Object> serializer = (Serializer<Object>) getSerializer(type)
            .orElseThrow(() -> new SerializationException("No serializer registered for type: " + type.getName()));
        
        serializer.serialize(object, output);
    }
    
    @Override
    public <T> T deserialize(Class<T> type, InputStream input) throws IOException, SerializationException {
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(input, "input cannot be null");
        
        Serializer<T> serializer = getSerializer(type)
            .orElseThrow(() -> new SerializationException("No serializer registered for type: " + type.getName()));
        
        return serializer.deserialize(input);
    }
    
    @Override
    public Set<Class<?>> getRegisteredTypes() {
        return new HashSet<>(serializers.keySet());
    }
    
    @Override
    public boolean unregisterSerializer(Class<?> type) {
        Objects.requireNonNull(type, "type cannot be null");
        return serializers.remove(type) != null;
    }
    
    @Override
    public void clear() {
        serializers.clear();
    }
}
