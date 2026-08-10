package jdk.aprismate.network;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Registry for network packets and their handlers.
 * <p>
 * The PacketRegistry manages the mapping between packet IDs and packet types,
 * and routes incoming packets to their registered handlers.
 * </p>
 *
 * @since 26.0-Alpha.7
 */
public interface PacketRegistry {
    
    /**
     * Registers a packet type with its factory.
     * <p>
     * The factory is used to create new packet instances for deserialization.
     * </p>
     *
     * @param <T> the packet type
     * @param id the packet identifier
     * @param factory factory to create packet instances
     * @throws NullPointerException if id or factory is null
     * @throws IllegalArgumentException if id is already registered
     */
    <T extends Packet> void registerPacket(PacketId id, Supplier<T> factory);
    
    /**
     * Registers a packet handler for client-bound packets.
     *
     * @param <T> the packet type
     * @param id the packet identifier
     * @param handler the packet handler
     * @throws NullPointerException if id or handler is null
     * @throws IllegalArgumentException if id is not registered
     */
    <T extends Packet> void registerClientHandler(PacketId id, PacketHandler<T> handler);
    
    /**
     * Registers a packet handler for server-bound packets.
     *
     * @param <T> the packet type
     * @param id the packet identifier
     * @param handler the packet handler
     * @throws NullPointerException if id or handler is null
     * @throws IllegalArgumentException if id is not registered
     */
    <T extends Packet> void registerServerHandler(PacketId id, PacketHandler<T> handler);
    
    /**
     * Creates a new packet instance for the given ID.
     *
     * @param id the packet identifier
     * @return a new packet instance, or empty if not registered
     * @throws NullPointerException if id is null
     */
    Optional<Packet> createPacket(PacketId id);
    
    /**
     * Returns the packet handler for the given ID and side.
     *
     * @param id the packet identifier
     * @param side the network side
     * @return the packet handler, or empty if not registered
     * @throws NullPointerException if id or side is null
     */
    Optional<PacketHandler<? extends Packet>> getHandler(PacketId id, NetworkSide side);
    
    /**
     * Checks if a packet type is registered.
     *
     * @param id the packet identifier
     * @return true if the packet type is registered
     * @throws NullPointerException if id is null
     */
    boolean isRegistered(PacketId id);
    
    /**
     * Returns all registered packet IDs.
     *
     * @return set of registered packet IDs
     */
    Set<PacketId> getRegisteredPackets();
    
    /**
     * Clears all registered packets and handlers.
     * <p>
     * This is primarily for testing. In production, registries should not
     * be cleared after initialization.
     * </p>
     */
    void clear();
}
