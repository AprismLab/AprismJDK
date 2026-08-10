package jdk.aprismate.network;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Tests for Packet interface.
 */
class PacketTest {
    
    @Test
    void packetShouldHaveId() {
        TestPacket packet = new TestPacket();
        assertNotNull(packet.getId());
    }
    
    @Test
    void packetShouldSupportWriting() throws IOException {
        TestPacket packet = new TestPacket();
        packet.value = 42;
        
        ByteBuffer buffer = ByteBuffer.allocate(256);
        packet.write(buffer);
        
        buffer.flip();
        assertEquals(42, buffer.getInt());
    }
    
    @Test
    void packetShouldSupportReading() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(256);
        buffer.putInt(42);
        buffer.flip();
        
        TestPacket packet = new TestPacket();
        packet.read(buffer);
        
        assertEquals(42, packet.value);
    }
    
    @Test
    void estimateSizeShouldReturnPositive() {
        TestPacket packet = new TestPacket();
        assertTrue(packet.estimateSize() > 0);
    }
    
    @Test
    void defaultIsClientBoundShouldBeFalse() {
        TestPacket packet = new TestPacket();
        assertFalse(packet.isClientBound());
    }
    
    @Test
    void defaultIsServerBoundShouldBeFalse() {
        TestPacket packet = new TestPacket();
        assertFalse(packet.isServerBound());
    }
    
    @Test
    void clientBoundPacketShouldReturnTrue() {
        ClientBoundPacket packet = new ClientBoundPacket();
        assertTrue(packet.isClientBound());
        assertFalse(packet.isServerBound());
    }
    
    @Test
    void serverBoundPacketShouldReturnTrue() {
        ServerBoundPacket packet = new ServerBoundPacket();
        assertFalse(packet.isClientBound());
        assertTrue(packet.isServerBound());
    }
    
    // Test packet implementations
    private static class TestPacket implements Packet {
        int value;
        
        @Override
        public PacketId getId() {
            return PacketId.of("test:packet");
        }
        
        @Override
        public void write(ByteBuffer buffer) {
            buffer.putInt(value);
        }
        
        @Override
        public void read(ByteBuffer buffer) {
            value = buffer.getInt();
        }
    }
    
    private static class ClientBoundPacket implements Packet {
        @Override
        public PacketId getId() {
            return PacketId.of("test:client");
        }
        
        @Override
        public void write(ByteBuffer buffer) {}
        
        @Override
        public void read(ByteBuffer buffer) {}
        
        @Override
        public boolean isClientBound() {
            return true;
        }
    }
    
    private static class ServerBoundPacket implements Packet {
        @Override
        public PacketId getId() {
            return PacketId.of("test:server");
        }
        
        @Override
        public void write(ByteBuffer buffer) {}
        
        @Override
        public void read(ByteBuffer buffer) {}
        
        @Override
        public boolean isServerBound() {
            return true;
        }
    }
}
