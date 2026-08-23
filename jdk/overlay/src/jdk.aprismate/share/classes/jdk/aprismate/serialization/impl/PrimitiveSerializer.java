package jdk.aprismate.serialization.impl;

import jdk.aprismate.serialization.Serializer;
import jdk.aprismate.serialization.SerializationException;

import java.io.*;
import java.util.Objects;

/**
 * A primitive serializer for common Java types.
 * <p>
 * Supports: Integer, Long, Double, Float, Boolean, Byte, Short, Character
 * </p>
 *
 * @since 26.0-Alpha.8
 */
public class PrimitiveSerializer<T> implements Serializer<T> {
    
    private final Class<T> type;
    
    public PrimitiveSerializer(Class<T> type) {
        this.type = Objects.requireNonNull(type, "type cannot be null");
        if (!isPrimitiveWrapper(type)) {
            throw new IllegalArgumentException("Not a primitive wrapper type: " + type.getName());
        }
    }
    
    private static boolean isPrimitiveWrapper(Class<?> type) {
        return type == Integer.class || type == Long.class || type == Double.class ||
               type == Float.class || type == Boolean.class || type == Byte.class ||
               type == Short.class || type == Character.class;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void serialize(T object, OutputStream output) throws IOException {
        DataOutputStream dos = new DataOutputStream(output);
        
        if (object instanceof Integer) {
            dos.writeInt((Integer) object);
        } else if (object instanceof Long) {
            dos.writeLong((Long) object);
        } else if (object instanceof Double) {
            dos.writeDouble((Double) object);
        } else if (object instanceof Float) {
            dos.writeFloat((Float) object);
        } else if (object instanceof Boolean) {
            dos.writeBoolean((Boolean) object);
        } else if (object instanceof Byte) {
            dos.writeByte((Byte) object);
        } else if (object instanceof Short) {
            dos.writeShort((Short) object);
        } else if (object instanceof Character) {
            dos.writeChar((Character) object);
        }
        
        dos.flush();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(InputStream input) throws IOException, SerializationException {
        DataInputStream dis = new DataInputStream(input);
        
        if (type == Integer.class) {
            return (T) Integer.valueOf(dis.readInt());
        } else if (type == Long.class) {
            return (T) Long.valueOf(dis.readLong());
        } else if (type == Double.class) {
            return (T) Double.valueOf(dis.readDouble());
        } else if (type == Float.class) {
            return (T) Float.valueOf(dis.readFloat());
        } else if (type == Boolean.class) {
            return (T) Boolean.valueOf(dis.readBoolean());
        } else if (type == Byte.class) {
            return (T) Byte.valueOf(dis.readByte());
        } else if (type == Short.class) {
            return (T) Short.valueOf(dis.readShort());
        } else if (type == Character.class) {
            return (T) Character.valueOf(dis.readChar());
        }
        
        throw new SerializationException("Unsupported type: " + type.getName());
    }
    
    @Override
    public Class<T> getType() {
        return type;
    }
    
    @Override
    public String getFormat() {
        return "binary";
    }
}
