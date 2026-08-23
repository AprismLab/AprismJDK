package jdk.aprismate.network;

/**
 * Handles incoming network packets.
 * <p>
 * PacketHandlers are registered with a {@link PacketRegistry} and invoked
 * when packets of their type are received. Handlers can process packets
 * on either the client or server side.
 * </p>
 *
 * @param <T> the packet type this handler processes
 * @since 26.0-Alpha.7
 */
@FunctionalInterface
public interface PacketHandler<T extends Packet> {
    
    /**
     * Handles an incoming packet.
     * <p>
     * This method is called when a packet is received and decoded.
     * The handler should process the packet's data and perform any
     * necessary actions.
     * </p>
     * <p>
     * Handlers should be fast and non-blocking. Long-running operations
     * should be offloaded to a separate thread or task queue.
     * </p>
     *
     * @param packet the received packet
     * @param context the network context providing information about the connection
     * @throws NullPointerException if packet or context is null
     */
    void handle(T packet, NetworkContext context);
}
