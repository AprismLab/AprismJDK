# Network API

## Overview

The AprismJDK Network API provides a high-level abstraction for client-server communication. It handles packet serialization, routing, connection management, and protocol negotiation, allowing mods and applications to focus on game logic rather than low-level networking.

## Core Concepts

### Packet

A unit of data sent between client and server. Each packet has a unique ID and can serialize/deserialize itself.

```java
public class ChatPacket implements Packet {
    private String message;
    private String sender;
    
    @Override
    public PacketId getId() {
        return PacketId.of("mymod:chat");
    }
    
    @Override
    public void write(ByteBuffer buffer) throws IOException {
        // Write message length and content
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        buffer.putInt(messageBytes.length);
        buffer.put(messageBytes);
        
        // Write sender
        byte[] senderBytes = sender.getBytes(StandardCharsets.UTF_8);
        buffer.putInt(senderBytes.length);
        buffer.put(senderBytes);
    }
    
    @Override
    public void read(ByteBuffer buffer) throws IOException {
        // Read message
        int messageLen = buffer.getInt();
        byte[] messageBytes = new byte[messageLen];
        buffer.get(messageBytes);
        message = new String(messageBytes, StandardCharsets.UTF_8);
        
        // Read sender
        int senderLen = buffer.getInt();
        byte[] senderBytes = new byte[senderLen];
        buffer.get(senderBytes);
        sender = new String(senderBytes, StandardCharsets.UTF_8);
    }
    
    @Override
    public boolean isServerBound() {
        return true; // Clients send chat to server
    }
    
    // Getters and setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
}
```

### PacketId

Unique identifier for packet types using namespace:path format.

```java
// Using default namespace
PacketId id1 = PacketId.of("handshake");  // aprism:handshake

// Using custom namespace
PacketId id2 = PacketId.of("mymod:chat");  // mymod:chat
PacketId id3 = new PacketId("mymod", "inventory_update");

// Properties
String namespace = id2.getNamespace();  // "mymod"
String path = id2.getPath();             // "chat"
boolean isDefault = id1.isDefaultNamespace();  // true
```

### PacketHandler

Processes incoming packets on client or server side.

```java
public class ChatPacketHandler implements PacketHandler<ChatPacket> {
    
    @Override
    public void handle(ChatPacket packet, NetworkContext context) {
        if (context.isServer()) {
            // Server received chat from client
            String message = packet.getMessage();
            String sender = packet.getSender();
            
            System.out.println("[" + sender + "] " + message);
            
            // Broadcast to all clients
            broadcastToAllClients(packet);
        } else {
            // Client received chat from server
            displayChatMessage(packet.getSender(), packet.getMessage());
        }
    }
}
```

### NetworkConnection

Represents an active connection between client and server.

```java
NetworkConnection connection = context.getConnection();

// Send packets
connection.send(new ChatPacket("Hello", "Player1"));

// Send asynchronously
connection.sendAsync(packet).thenRun(() -> {
    System.out.println("Packet sent");
}).exceptionally(ex -> {
    System.err.println("Failed to send: " + ex.getMessage());
    return null;
});

// Connection info
SocketAddress remote = connection.getRemoteAddress();
boolean active = connection.isActive();
long sentPackets = connection.getSentPackets();
long receivedBytes = connection.getReceivedBytes();

// Close connection
connection.close("Client disconnected");
```

### NetworkContext

Provides context for packet handling, including connection and metadata.

```java
public void handle(MyPacket packet, NetworkContext context) {
    // Check side
    if (context.isClient()) {
        // Handle on client
    } else if (context.isServer()) {
        // Handle on server
    }
    
    // Get connection
    NetworkConnection conn = context.getConnection();
    
    // Reply with another packet
    context.reply(new ResponsePacket());
    
    // Access player (if in game context)
    context.getPlayer().ifPresent(player -> {
        // Do something with player
    });
    
    // Store/retrieve metadata
    context.setMetadata("lastPacketTime", System.currentTimeMillis());
    Long lastTime = (Long) context.getMetadata("lastPacketTime").orElse(0L);
}
```

### NetworkSide

Indicates which side of the connection (client or server).

```java
NetworkSide side = context.getSide();

if (side == NetworkSide.CLIENT) {
    // Client-side logic
} else if (side == NetworkSide.SERVER) {
    // Server-side logic
}

// Utility methods
boolean isClient = side.isClient();
boolean isServer = side.isServer();
NetworkSide opposite = side.opposite();  // CLIENT <-> SERVER
```

### PacketRegistry

Central registry for packet types and handlers.

```java
PacketRegistry registry = Packets.getRegistry();

// Register packet type
registry.registerPacket(
    PacketId.of("mymod:chat"),
    ChatPacket::new  // Factory method
);

// Register handler for server-bound packets
registry.registerServerHandler(
    PacketId.of("mymod:chat"),
    new ChatPacketHandler()
);

// Register handler for client-bound packets
registry.registerClientHandler(
    PacketId.of("mymod:broadcast"),
    new BroadcastHandler()
);

// Query registry
boolean registered = registry.isRegistered(PacketId.of("mymod:chat"));
Set<PacketId> allPackets = registry.getRegisteredPackets();
```

## Usage Examples

### Simple Request-Response

```java
// Request packet (client -> server)
public class PingPacket implements Packet {
    private long timestamp;
    
    @Override
    public PacketId getId() {
        return PacketId.of("mymod:ping");
    }
    
    @Override
    public void write(ByteBuffer buffer) {
        buffer.putLong(timestamp);
    }
    
    @Override
    public void read(ByteBuffer buffer) {
        timestamp = buffer.getLong();
    }
    
    @Override
    public boolean isServerBound() {
        return true;
    }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}

// Response packet (server -> client)
public class PongPacket implements Packet {
    private long clientTime;
    private long serverTime;
    
    @Override
    public PacketId getId() {
        return PacketId.of("mymod:pong");
    }
    
    @Override
    public void write(ByteBuffer buffer) {
        buffer.putLong(clientTime);
        buffer.putLong(serverTime);
    }
    
    @Override
    public void read(ByteBuffer buffer) {
        clientTime = buffer.getLong();
        serverTime = buffer.getLong();
    }
    
    @Override
    public boolean isClientBound() {
        return true;
    }
    
    public long getClientTime() { return clientTime; }
    public void setClientTime(long time) { this.clientTime = time; }
    public long getServerTime() { return serverTime; }
    public void setServerTime(long time) { this.serverTime = time; }
}

// Server handler
public class PingHandler implements PacketHandler<PingPacket> {
    @Override
    public void handle(PingPacket packet, NetworkContext context) {
        PongPacket response = new PongPacket();
        response.setClientTime(packet.getTimestamp());
        response.setServerTime(System.currentTimeMillis());
        context.reply(response);
    }
}

// Client handler
public class PongHandler implements PacketHandler<PongPacket> {
    @Override
    public void handle(PongPacket packet, NetworkContext context) {
        long now = System.currentTimeMillis();
        long roundTrip = now - packet.getClientTime();
        System.out.println("Ping: " + roundTrip + "ms");
    }
}

// Registration
PacketRegistry registry = Packets.getRegistry();
registry.registerPacket(PacketId.of("mymod:ping"), PingPacket::new);
registry.registerPacket(PacketId.of("mymod:pong"), PongPacket::new);
registry.registerServerHandler(PacketId.of("mymod:ping"), new PingHandler());
registry.registerClientHandler(PacketId.of("mymod:pong"), new PongHandler());

// Client sends ping
PingPacket ping = new PingPacket();
ping.setTimestamp(System.currentTimeMillis());
connection.send(ping);
```

### Broadcasting to Multiple Clients

```java
public class BroadcastPacket implements Packet {
    private String message;
    
    @Override
    public PacketId getId() {
        return PacketId.of("mymod:broadcast");
    }
    
    @Override
    public void write(ByteBuffer buffer) throws IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        buffer.putInt(bytes.length);
        buffer.put(bytes);
    }
    
    @Override
    public void read(ByteBuffer buffer) throws IOException {
        int len = buffer.getInt();
        byte[] bytes = new byte[len];
        buffer.get(bytes);
        message = new String(bytes, StandardCharsets.UTF_8);
    }
    
    @Override
    public boolean isClientBound() {
        return true;
    }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

// Server broadcasts to all connected clients
public void broadcastMessage(String message, List<NetworkConnection> clients) {
    BroadcastPacket packet = new BroadcastPacket();
    packet.setMessage(message);
    
    for (NetworkConnection client : clients) {
        if (client.isActive()) {
            client.send(packet);
        }
    }
}
```

### Complex Data Structures

```java
public class InventoryUpdatePacket implements Packet {
    private Map<Integer, ItemStack> items;
    
    @Override
    public PacketId getId() {
        return PacketId.of("mymod:inventory_update");
    }
    
    @Override
    public void write(ByteBuffer buffer) throws IOException {
        buffer.putInt(items.size());
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            buffer.putInt(entry.getKey());
            writeItemStack(buffer, entry.getValue());
        }
    }
    
    @Override
    public void read(ByteBuffer buffer) throws IOException {
        items = new HashMap<>();
        int size = buffer.getInt();
        for (int i = 0; i < size; i++) {
            int slot = buffer.getInt();
            ItemStack item = readItemStack(buffer);
            items.put(slot, item);
        }
    }
    
    private void writeItemStack(ByteBuffer buffer, ItemStack item) {
        // Write item ID
        byte[] idBytes = item.getId().getBytes(StandardCharsets.UTF_8);
        buffer.putInt(idBytes.length);
        buffer.put(idBytes);
        
        // Write count
        buffer.putInt(item.getCount());
    }
    
    private ItemStack readItemStack(ByteBuffer buffer) {
        // Read item ID
        int idLen = buffer.getInt();
        byte[] idBytes = new byte[idLen];
        buffer.get(idBytes);
        String id = new String(idBytes, StandardCharsets.UTF_8);
        
        // Read count
        int count = buffer.getInt();
        
        return new ItemStack(id, count);
    }
    
    @Override
    public boolean isClientBound() {
        return true;
    }
    
    public Map<Integer, ItemStack> getItems() { return items; }
    public void setItems(Map<Integer, ItemStack> items) { this.items = items; }
}
```

### Connection Lifecycle

```java
public class ConnectionHandler {
    
    public void onClientConnected(NetworkConnection connection) {
        System.out.println("Client connected from: " + connection.getRemoteAddress());
        
        // Send welcome packet
        WelcomePacket welcome = new WelcomePacket();
        welcome.setServerName("My Server");
        welcome.setMotd("Welcome to the server!");
        connection.send(welcome);
    }
    
    public void onClientDisconnected(NetworkConnection connection, String reason) {
        System.out.println("Client disconnected: " + reason);
        System.out.println("Stats:");
        System.out.println("  Sent: " + connection.getSentPackets() + " packets, " + 
                         connection.getSentBytes() + " bytes");
        System.out.println("  Received: " + connection.getReceivedPackets() + " packets, " + 
                         connection.getReceivedBytes() + " bytes");
    }
    
    public void kickClient(NetworkConnection connection, String reason) {
        DisconnectPacket packet = new DisconnectPacket();
        packet.setReason(reason);
        
        // Send disconnect packet and close
        connection.sendAsync(packet).thenRun(() -> {
            connection.close(reason);
        });
    }
}
```

### Packet Size Estimation

```java
public class LargeDataPacket implements Packet {
    private byte[] data;
    
    @Override
    public PacketId getId() {
        return PacketId.of("mymod:large_data");
    }
    
    @Override
    public void write(ByteBuffer buffer) throws IOException {
        buffer.putInt(data.length);
        buffer.put(data);
    }
    
    @Override
    public void read(ByteBuffer buffer) throws IOException {
        int len = buffer.getInt();
        data = new byte[len];
        buffer.get(data);
    }
    
    @Override
    public int estimateSize() {
        // 4 bytes for length + actual data size
        return 4 + (data != null ? data.length : 0);
    }
    
    public byte[] getData() { return data; }
    public void setData(byte[] data) { this.data = data; }
}
```

## Best Practices

### 1. Use Meaningful Packet IDs

```java
// Good - clear and namespaced
PacketId.of("mymod:player_move")
PacketId.of("mymod:inventory_update")
PacketId.of("mymod:chat_message")

// Bad - unclear or likely to conflict
PacketId.of("packet1")
PacketId.of("update")
PacketId.of("data")
```

### 2. Validate Packet Data

```java
@Override
public void read(ByteBuffer buffer) throws IOException {
    int len = buffer.getInt();
    
    // Validate length to prevent attacks
    if (len < 0 || len > 65536) {
        throw new IOException("Invalid string length: " + len);
    }
    
    byte[] bytes = new byte[len];
    buffer.get(bytes);
    message = new String(bytes, StandardCharsets.UTF_8);
}
```

### 3. Handle Errors Gracefully

```java
@Override
public void handle(MyPacket packet, NetworkContext context) {
    try {
        // Process packet
        processPacket(packet);
    } catch (Exception e) {
        System.err.println("Error handling packet: " + e.getMessage());
        e.printStackTrace();
        
        // Send error response
        ErrorPacket error = new ErrorPacket();
        error.setMessage("Failed to process request");
        context.reply(error);
    }
}
```

### 4. Provide Accurate Size Estimates

```java
@Override
public int estimateSize() {
    int size = 0;
    
    // PacketId overhead
    size += 8;
    
    // String fields (4 bytes length + content)
    if (message != null) {
        size += 4 + message.getBytes(StandardCharsets.UTF_8).length;
    }
    
    // Primitive fields
    size += 4; // int
    size += 8; // long
    
    return size;
}
```

### 5. Use Async Sending for Large Packets

```java
// Small packets - synchronous is fine
connection.send(smallPacket);

// Large packets - use async
connection.sendAsync(largePacket).thenRun(() -> {
    System.out.println("Large packet sent");
}).exceptionally(ex -> {
    System.err.println("Failed to send: " + ex.getMessage());
    return null;
});
```

### 6. Clean Up Resources

```java
@Override
public void handle(FileTransferPacket packet, NetworkContext context) {
    InputStream stream = null;
    try {
        stream = packet.openStream();
        // Process stream
    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
}
```

## Implementation Status (v26.0-Alpha.7)

### Completed
- ✅ Packet interface with serialization methods
- ✅ PacketId with namespace:path format
- ✅ PacketHandler functional interface
- ✅ NetworkContext for packet handling context
- ✅ NetworkSide enum (client/server)
- ✅ NetworkConnection interface
- ✅ PacketRegistry interface
- ✅ Packets static API for global access
- ✅ Comprehensive test suite (184 tests)
- ✅ Documentation

### Not Yet Implemented (Alpha.7)
- ⏳ Concrete NetworkConnection implementation
- ⏳ Concrete PacketRegistry implementation
- ⏳ Network protocol implementation
- ⏳ Packet encoding/decoding
- ⏳ Connection pooling
- ⏳ Packet compression
- ⏳ Packet encryption
- ⏳ Rate limiting
- ⏳ Connection timeout handling
- ⏳ Packet fragmentation for large packets

The API is complete and tested. Implementation will be added in aprismate-agent in future releases.

## Design Principles

1. **Type Safety** - Strongly typed packets with compile-time checks
2. **Bidirectional** - Support both client->server and server->client packets
3. **Extensible** - Easy to add new packet types
4. **Efficient** - Direct ByteBuffer serialization without intermediate allocations
5. **Flexible** - Support various packet sizes and data types
6. **Reliable** - Built-in error handling and validation

## Security Considerations

- Always validate packet data lengths to prevent buffer overflow attacks
- Implement rate limiting to prevent packet flooding
- Use encryption for sensitive data
- Validate packet sources on the server side
- Implement timeouts to prevent connection exhaustion
- Log suspicious packet patterns for monitoring

## Testing

Network components should be tested for:
- Packet serialization/deserialization round-trips
- Packet ID uniqueness and format
- Handler registration and invocation
- Connection state management
- Error handling for invalid data
- Thread safety for concurrent operations

See test classes in `jdk.aprismate.network` package for examples.

## Future Enhancements (Post-v26.0)

- Automatic packet versioning and compatibility checking
- Packet priority queues
- Bandwidth monitoring and throttling
- Automatic reconnection logic
- Packet batching for efficiency
- Built-in compression and encryption
- Network debugging tools
- Packet replay for testing
- Network simulation (latency, packet loss)
