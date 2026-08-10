package jdk.aprismate.network;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Tests for PacketHandler interface.
 */
class PacketHandlerTest {
    
    @Test
    void handlerShouldReceivePacket() {
        TestPacket packet = new TestPacket();
        packet.value = 42;
        
        TestContext context = new TestContext();
        TestHandler handler = new TestHandler();
        
        handler.handle(packet, context);
        
        assertEquals(42, handler.receivedValue);
        assertSame(context, handler.receivedContext);
    }
    
    @Test
    void handlerShouldBeCallable() {
        PacketHandler<TestPacket> handler = (packet, context) -> {
            // Handler logic
        };
        
        assertDoesNotThrow(() -> {
            handler.handle(new TestPacket(), new TestContext());
        });
    }
    
    @Test
    void multipleHandlersShouldBeIndependent() {
        TestPacket packet = new TestPacket();
        packet.value = 100;
        
        TestContext context = new TestContext();
        
        TestHandler handler1 = new TestHandler();
        TestHandler handler2 = new TestHandler();
        
        handler1.handle(packet, context);
        handler2.handle(packet, context);
        
        assertEquals(100, handler1.receivedValue);
        assertEquals(100, handler2.receivedValue);
    }
    
    // Test implementations
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
    
    private static class TestHandler implements PacketHandler<TestPacket> {
        int receivedValue;
        NetworkContext receivedContext;
        
        @Override
        public void handle(TestPacket packet, NetworkContext context) {
            receivedValue = packet.value;
            receivedContext = context;
        }
    }
    
    private static class TestContext implements NetworkContext {
        @Override
        public NetworkSide getSide() {
            return NetworkSide.SERVER;
        }
        
        @Override
        public NetworkConnection getConnection() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public java.util.Optional<Object> getMetadata(String key) {
            return java.util.Optional.empty();
        }
        
        @Override
        public void setMetadata(String key, Object value) {
        }
    }
}
