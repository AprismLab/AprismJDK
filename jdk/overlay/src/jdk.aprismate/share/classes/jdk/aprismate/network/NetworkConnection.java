package jdk.aprismate.network;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * Represents an active network connection.
 * <p>
 * A NetworkConnection represents a bidirectional communication channel
 * between a client and server. It provides methods to send packets,
 * query connection state, and manage the connection lifecycle.
 * </p>
 *
 * @since 26.0-Alpha.7
 */
public interface NetworkConnection {
    
    /**
     * Sends a packet on this connection.
     * <p>
     * The packet will be queued for transmission. This method returns
     * immediately without waiting for the packet to be sent.
     * </p>
     *
     * @param packet the packet to send
     * @throws NullPointerException if packet is null
     * @throws IllegalStateException if connection is not active
     */
    void send(Packet packet);
    
    /**
     * Sends a packet and returns a future that completes when sent.
     * <p>
     * The returned future will complete successfully when the packet
     * has been written to the network, or exceptionally if sending fails.
     * </p>
     *
     * @param packet the packet to send
     * @return a future that completes when the packet is sent
     * @throws NullPointerException if packet is null
     */
    CompletableFuture<Void> sendAsync(Packet packet);
    
    /**
     * Closes this connection.
     * <p>
     * Any pending packets will be flushed before closing. After this
     * method returns, no more packets can be sent or received.
     * </p>
     */
    void close();
    
    /**
     * Closes this connection with a reason.
     *
     * @param reason the reason for closing
     * @throws NullPointerException if reason is null
     */
    void close(String reason);
    
    /**
     * Checks if this connection is active.
     *
     * @return true if the connection is open and can send/receive packets
     */
    boolean isActive();
    
    /**
     * Returns the remote address of this connection.
     *
     * @return the remote socket address
     */
    SocketAddress getRemoteAddress();
    
    /**
     * Returns the local address of this connection.
     *
     * @return the local socket address
     */
    SocketAddress getLocalAddress();
    
    /**
     * Returns the side this connection represents.
     *
     * @return the network side
     */
    NetworkSide getSide();
    
    /**
     * Checks if this connection is encrypted.
     *
     * @return true if the connection uses encryption
     */
    default boolean isEncrypted() {
        return false;
    }
    
    /**
     * Checks if this connection is compressed.
     *
     * @return true if packets are compressed
     */
    default boolean isCompressed() {
        return false;
    }
    
    /**
     * Returns the number of packets sent on this connection.
     *
     * @return the sent packet count
     */
    long getSentPackets();
    
    /**
     * Returns the number of packets received on this connection.
     *
     * @return the received packet count
     */
    long getReceivedPackets();
    
    /**
     * Returns the number of bytes sent on this connection.
     *
     * @return the sent byte count
     */
    long getSentBytes();
    
    /**
     * Returns the number of bytes received on this connection.
     *
     * @return the received byte count
     */
    long getReceivedBytes();
}
