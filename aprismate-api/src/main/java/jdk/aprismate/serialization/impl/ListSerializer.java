package jdk.aprismate.serialization.impl;

import jdk.aprismate.serialization.Serializer;
import jdk.aprismate.serialization.SerializationException;
import jdk.aprismate.serialization.Serializers;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A serializer for lists of objects.
 * <p>
 * This serializer delegates to the registry to serialize individual elements.
 * All elements must be of the same type.
 * </p>
 *
 * @param <T> the element type
 * @since 26.0-Alpha.8
 */
public class ListSerializer<T> implements Serializer<List<T>> {
    
    private final Class<T> elementType;
    
    public ListSerializer(Class<T> elementType) {
        this.elementType = elementType;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void serialize(List<T> object, OutputStream output) throws IOException {
        DataOutputStream dos = new DataOutputStream(output);
        dos.writeInt(object.size());
        
        for (T element : object) {
            try {
                Serializers.getRegistry().serialize(element, output);
            } catch (SerializationException e) {
                throw new IOException("Failed to serialize list element", e);
            }
        }
        
        dos.flush();
    }
    
    @Override
    public List<T> deserialize(InputStream input) throws IOException, SerializationException {
        DataInputStream dis = new DataInputStream(input);
        int size = dis.readInt();
        
        if (size < 0) {
            throw new SerializationException("Invalid list size: " + size);
        }
        
        if (size > 1_000_000) { // 1M element limit
            throw new SerializationException("List too large: " + size + " elements");
        }
        
        List<T> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            T element = Serializers.getRegistry().deserialize(elementType, input);
            list.add(element);
        }
        
        return list;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Class<List<T>> getType() {
        return (Class<List<T>>) (Class<?>) List.class;
    }
    
    @Override
    public String getFormat() {
        return "binary";
    }
    
    /**
     * Returns the element type.
     *
     * @return the element type
     */
    public Class<T> getElementType() {
        return elementType;
    }
}
