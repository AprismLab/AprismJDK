package jdk.aprismate.serialization.impl;

import jdk.aprismate.serialization.Serializer;
import jdk.aprismate.serialization.SerializationException;
import jdk.aprismate.serialization.Serializers;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * A serializer for maps with string keys.
 * <p>
 * This serializer delegates to the registry to serialize values.
 * Keys must be strings, values must be of the same type.
 * </p>
 *
 * @param <V> the value type
 * @since 26.0-Alpha.8
 */
public class MapSerializer<V> implements Serializer<Map<String, V>> {
    
    private final Class<V> valueType;
    
    public MapSerializer(Class<V> valueType) {
        this.valueType = valueType;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void serialize(Map<String, V> object, OutputStream output) throws IOException {
        DataOutputStream dos = new DataOutputStream(output);
        dos.writeInt(object.size());
        
        for (Map.Entry<String, V> entry : object.entrySet()) {
            // Serialize key
            try {
                Serializers.getRegistry().serialize(entry.getKey(), output);
                Serializers.getRegistry().serialize(entry.getValue(), output);
            } catch (SerializationException e) {
                throw new IOException("Failed to serialize map entry", e);
            }
        }
        
        dos.flush();
    }
    
    @Override
    public Map<String, V> deserialize(InputStream input) throws IOException, SerializationException {
        DataInputStream dis = new DataInputStream(input);
        int size = dis.readInt();
        
        if (size < 0) {
            throw new SerializationException("Invalid map size: " + size);
        }
        
        if (size > 1_000_000) { // 1M entry limit
            throw new SerializationException("Map too large: " + size + " entries");
        }
        
        Map<String, V> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            String key = Serializers.getRegistry().deserialize(String.class, input);
            V value = Serializers.getRegistry().deserialize(valueType, input);
            map.put(key, value);
        }
        
        return map;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Class<Map<String, V>> getType() {
        return (Class<Map<String, V>>) (Class<?>) Map.class;
    }
    
    @Override
    public String getFormat() {
        return "binary";
    }
    
    /**
     * Returns the value type.
     *
     * @return the value type
     */
    public Class<V> getValueType() {
        return valueType;
    }
}
