package jdk.aprismate.network;

import java.util.Optional;

/**
 * Provides context information for packet handling.
 * <p>
 * The NetworkContext gives packet handlers access to information about
 * the connection, the sender, and provides methods to respond with packets.
 * </p>
 *
 * @since 26.0-Alpha.7
 */
public interface NetworkContext {
    
    /**
     * Returns the side this context represents.
     *
     * @return the network side (client or server)
     */
    NetworkSide getSide();
    
    /**
     * Checks if this context is on the client side.
     *
     * @return true if this is a client context
     */
    default boolean isClient() {
        return getSide() == NetworkSide.CLIENT;
    }
    
    /**
     * Checks if this context is on the server side.
     *
     * @return true if this is a server context
     */
    default boolean isServer() {
        return getSide() == NetworkSide.SERVER;
    }
    
    /**
     * Returns the connection this packet was received on.
     *
     * @return the network connection, never null
     */
    NetworkConnection getConnection();
    
    /**
     * Sends a packet in response.
     * <p>
     * This is a convenience method equivalent to
     * {@code getConnection().send(packet)}.
     * </p>
     *
     * @param packet the packet to send
     * @throws NullPointerException if packet is null
     */
    default void reply(Packet packet) {
        getConnection().send(packet);
    }
    
    /**
     * Returns the player who sent this packet, if available.
     * <p>
     * This is only available in game contexts where the concept of
     * a player exists. Returns empty for system-level packets or
     * during early connection phases.
     * </p>
     *
     * @return the player, or empty if not applicable
     */
    default Optional<Object> getPlayer() {
        return Optional.empty();
    }
    
    /**
     * Returns custom metadata associated with this context.
     *
     * @param key the metadata key
     * @return the metadata value, or empty if not set
     * @throws NullPointerException if key is null
     */
    Optional<Object> getMetadata(String key);
    
    /**
     * Sets custom metadata for this context.
     *
     * @param key the metadata key
     * @param value the metadata value (null to remove)
     * @throws NullPointerException if key is null
     */
    void setMetadata(String key, Object value);
}
