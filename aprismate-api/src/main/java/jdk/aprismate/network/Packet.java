package jdk.aprismate.network;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Represents a network packet that can be sent between client and server.
 * <p>
 * Packets are the fundamental unit of network communication in AprismJDK.
 * Each packet has a unique identifier and carries data that can be serialized
 * and deserialized.
 * </p>
 *
 * @since 26.0-Alpha.7
 */
public interface Packet {
    
    /**
     * Returns the unique identifier for this packet type.
     * <p>
     * The packet ID is used to identify which packet handler should process
     * this packet. IDs should be consistent between client and server.
     * </p>
     *
     * @return the packet identifier, never null
     */
    PacketId getId();
    
    /**
     * Writes the packet data to a buffer.
     * <p>
     * This method serializes the packet's fields into the provided buffer.
     * The buffer's position will be advanced by the number of bytes written.
     * </p>
     *
     * @param buffer the buffer to write to
     * @throws IOException if writing fails
     * @throws NullPointerException if buffer is null
     */
    void write(ByteBuffer buffer) throws IOException;
    
    /**
     * Reads the packet data from a buffer.
     * <p>
     * This method deserializes the packet's fields from the provided buffer.
     * The buffer's position will be advanced by the number of bytes read.
     * </p>
     *
     * @param buffer the buffer to read from
     * @throws IOException if reading fails or data is invalid
     * @throws NullPointerException if buffer is null
     */
    void read(ByteBuffer buffer) throws IOException;
    
    /**
     * Returns the estimated size of this packet in bytes.
     * <p>
     * This is used for buffer allocation and may be approximate. The actual
     * size may be smaller or larger depending on variable-length data.
     * </p>
     *
     * @return the estimated packet size in bytes, must be positive
     */
    default int estimateSize() {
        return 256; // Default estimate
    }
    
    /**
     * Checks if this packet should be processed on the client side.
     *
     * @return true if this packet can be received by clients
     */
    default boolean isClientBound() {
        return false;
    }
    
    /**
     * Checks if this packet should be processed on the server side.
     *
     * @return true if this packet can be received by servers
     */
    default boolean isServerBound() {
        return false;
    }
}
