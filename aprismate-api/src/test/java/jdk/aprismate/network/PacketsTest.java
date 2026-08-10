package jdk.aprismate.network;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Packets static API.
 */
class PacketsTest {
    
    @AfterEach
    void cleanup() {
        Packets.clear();
    }
    
    @Test
    void isInitializedShouldReturnFalseInitially() {
        assertFalse(Packets.isInitialized());
    }
    
    @Test
    void setRegistryShouldInitialize() {
        PacketRegistry registry = new TestPacketRegistry();
        Packets.setRegistry(registry);
        assertTrue(Packets.isInitialized());
    }
    
    @Test
    void setRegistryShouldRejectNull() {
        assertThrows(NullPointerException.class, () -> {
            Packets.setRegistry(null);
        });
    }
    
    @Test
    void setRegistryShouldRejectDuplicateInitialization() {
        Packets.setRegistry(new TestPacketRegistry());
        assertThrows(IllegalStateException.class, () -> {
            Packets.setRegistry(new TestPacketRegistry());
        });
    }
    
    @Test
    void getRegistryShouldReturnSetRegistry() {
        PacketRegistry registry = new TestPacketRegistry();
        Packets.setRegistry(registry);
        assertSame(registry, Packets.getRegistry());
    }
    
    @Test
    void getRegistryShouldThrowIfNotInitialized() {
        assertThrows(IllegalStateException.class, () -> {
            Packets.getRegistry();
        });
    }
    
    @Test
    void clearShouldResetState() {
        Packets.setRegistry(new TestPacketRegistry());
        assertTrue(Packets.isInitialized());
        
        Packets.clear();
        assertFalse(Packets.isInitialized());
    }
    
    @Test
    void cannotInstantiatePackets() throws Exception {
        java.lang.reflect.Constructor<?> constructor = 
            Packets.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        
        java.lang.reflect.InvocationTargetException exception = 
            assertThrows(java.lang.reflect.InvocationTargetException.class, () -> {
                constructor.newInstance();
            });
        
        assertTrue(exception.getCause() instanceof UnsupportedOperationException);
    }
    
    // Test implementation of PacketRegistry
    private static class TestPacketRegistry implements PacketRegistry {
        @Override
        public <T extends Packet> void registerPacket(PacketId id, java.util.function.Supplier<T> factory) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public <T extends Packet> void registerClientHandler(PacketId id, PacketHandler<T> handler) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public <T extends Packet> void registerServerHandler(PacketId id, PacketHandler<T> handler) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public java.util.Optional<Packet> createPacket(PacketId id) {
            return java.util.Optional.empty();
        }
        
        @Override
        public java.util.Optional<PacketHandler<? extends Packet>> getHandler(PacketId id, NetworkSide side) {
            return java.util.Optional.empty();
        }
        
        @Override
        public boolean isRegistered(PacketId id) {
            return false;
        }
        
        @Override
        public java.util.Set<PacketId> getRegisteredPackets() {
            return java.util.Collections.emptySet();
        }
        
        @Override
        public void clear() {
        }
    }
}
