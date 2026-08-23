package jdk.aprismate.serialization.impl;

import jdk.aprismate.serialization.Serializer;
import jdk.aprismate.serialization.SerializationException;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * A simple string serializer using UTF-8 encoding.
 *
 * @since 26.0-Alpha.8
 */
public class StringSerializer implements Serializer<String> {

    public StringSerializer() {}
    
    @Override
    public void serialize(String object, OutputStream output) throws IOException {
        byte[] bytes = object.getBytes(StandardCharsets.UTF_8);
        DataOutputStream dos = new DataOutputStream(output);
        dos.writeInt(bytes.length);
        dos.write(bytes);
        dos.flush();
    }
    
    @Override
    public String deserialize(InputStream input) throws IOException, SerializationException {
        DataInputStream dis = new DataInputStream(input);
        int length = dis.readInt();
        
        if (length < 0) {
            throw new SerializationException("Invalid string length: " + length);
        }
        
        if (length > 10_000_000) { // 10 MB limit
            throw new SerializationException("String too large: " + length + " bytes");
        }
        
        byte[] bytes = new byte[length];
        dis.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
    
    @Override
    public Class<String> getType() {
        return String.class;
    }
    
    @Override
    public String getFormat() {
        return "utf8";
    }
}
