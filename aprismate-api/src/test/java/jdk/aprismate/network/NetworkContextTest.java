package jdk.aprismate.network;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

/**
 * Tests for NetworkContext interface.
 */
class NetworkContextTest {
    
    @Test
    void getSideShouldReturnNetworkSide() {
        TestContext context = new TestContext(NetworkSide.CLIENT);
        assertEquals(NetworkSide.CLIENT, context.getSide());
        
        context = new TestContext(NetworkSide.SERVER);
        assertEquals(NetworkSide.SERVER, context.getSide());
    }
    
    @Test
    void isClientShouldReturnTrueForClientSide() {
        TestContext context = new TestContext(NetworkSide.CLIENT);
        assertTrue(context.isClient());
        assertFalse(context.isServer());
    }
    
    @Test
    void isServerShouldReturnTrueForServerSide() {
        TestContext context = new TestContext(NetworkSide.SERVER);
        assertTrue(context.isServer());
        assertFalse(context.isClient());
    }
    
    @Test
    void getConnectionShouldReturnConnection() {
        TestConnection connection = new TestConnection();
        TestContext context = new TestContext(NetworkSide.SERVER);
        context.connection = connection;
        
        assertSame(connection, context.getConnection());
    }
    
    @Test
    void getPlayerShouldReturnEmptyByDefault() {
        TestContext context = new TestContext(NetworkSide.SERVER);
        assertFalse(context.getPlayer().isPresent());
    }
    
    @Test
    void metadataShouldBeSettableAndRetrievable() {
        TestContext context = new TestContext(NetworkSide.SERVER);
        
        context.setMetadata("key1", "value1");
        context.setMetadata("key2", 42);
        
        assertEquals("value1", context.getMetadata("key1").orElse(null));
        assertEquals(42, context.getMetadata("key2").orElse(null));
    }
    
    @Test
    void metadataShouldReturnEmptyWhenNotSet() {
        TestContext context = new TestContext(NetworkSide.SERVER);
        assertFalse(context.getMetadata("nonexistent").isPresent());
    }
    
    @Test
    void metadataShouldBeRemovableWithNull() {
        TestContext context = new TestContext(NetworkSide.SERVER);
        context.setMetadata("key", "value");
        assertTrue(context.getMetadata("key").isPresent());
        
        context.setMetadata("key", null);
        assertFalse(context.getMetadata("key").isPresent());
    }
    
    // Test implementations
    private static class TestContext implements NetworkContext {
        private final NetworkSide side;
        NetworkConnection connection;
        private final java.util.Map<String, Object> metadata = new java.util.HashMap<>();
        
        TestContext(NetworkSide side) {
            this.side = side;
        }
        
        @Override
        public NetworkSide getSide() {
            return side;
        }
        
        @Override
        public NetworkConnection getConnection() {
            return connection;
        }
        
        @Override
        public Optional<Object> getMetadata(String key) {
            return Optional.ofNullable(metadata.get(key));
        }
        
        @Override
        public void setMetadata(String key, Object value) {
            if (value == null) {
                metadata.remove(key);
            } else {
                metadata.put(key, value);
            }
        }
    }
    
    private static class TestConnection implements NetworkConnection {
        @Override
        public void send(Packet packet) {}
        
        @Override
        public java.util.concurrent.CompletableFuture<Void> sendAsync(Packet packet) {
            return java.util.concurrent.CompletableFuture.completedFuture(null);
        }
        
        @Override
        public void close() {}
        
        @Override
        public void close(String reason) {}
        
        @Override
        public boolean isActive() {
            return true;
        }
        
        @Override
        public java.net.SocketAddress getRemoteAddress() {
            return null;
        }
        
        @Override
        public java.net.SocketAddress getLocalAddress() {
            return null;
        }
        
        @Override
        public NetworkSide getSide() {
            return NetworkSide.SERVER;
        }
        
        @Override
        public long getSentPackets() {
            return 0;
        }
        
        @Override
        public long getReceivedPackets() {
            return 0;
        }
        
        @Override
        public long getSentBytes() {
            return 0;
        }
        
        @Override
        public long getReceivedBytes() {
            return 0;
        }
    }
}
