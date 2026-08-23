package jdk.aprismate.network;

/**
 * Represents which side of the network connection this is.
 *
 * @since 26.0-Alpha.7
 */
public enum NetworkSide {
    
    /**
     * The client side of a connection.
     * <p>
     * Clients connect to servers and receive client-bound packets.
     * </p>
     */
    CLIENT,
    
    /**
     * The server side of a connection.
     * <p>
     * Servers accept connections from clients and receive server-bound packets.
     * </p>
     */
    SERVER;
    
    /**
     * Returns the opposite side.
     *
     * @return the opposite network side
     */
    public NetworkSide opposite() {
        return this == CLIENT ? SERVER : CLIENT;
    }
    
    /**
     * Checks if this is the client side.
     *
     * @return true if this is CLIENT
     */
    public boolean isClient() {
        return this == CLIENT;
    }
    
    /**
     * Checks if this is the server side.
     *
     * @return true if this is SERVER
     */
    public boolean isServer() {
        return this == SERVER;
    }
}
