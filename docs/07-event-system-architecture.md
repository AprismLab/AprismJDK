# Event System Architecture

## Overview

The AprismJDK event system provides a flexible, type-safe mechanism for mods to respond to events in the application lifecycle and runtime. The system is designed to be simple to use while providing powerful features like event phases, cancellation, and annotation-based registration.

## Core Components

### Event Interface

The base interface for all events. Events are immutable data objects that represent something that has happened or is about to happen.

```java
public interface Event {
    default boolean isCancellable();
    default EventPhase getPhase();
}
```

### CancellableEvent

Events that can be cancelled by listeners to prevent their default behavior.

```java
public interface CancellableEvent extends Event {
    boolean isCancelled();
    void setCancelled(boolean cancelled);
}
```

### EventPhase

Events are dispatched in three phases to control listener execution order:

- **EARLY** - Runs before all other phases (for setup and preprocessing)
- **DEFAULT** - The standard phase for most listeners
- **LATE** - Runs after all other phases (for cleanup and post-processing)

### EventBus

The central registry and dispatcher for events.

```java
// Register a listener
EventBus.register(MyEvent.class, event -> {
    // Handle event
});

// Fire an event
EventBus.fire(new MyEvent());
```

### @Subscribe Annotation

Annotation-based event listener registration:

```java
public class MyMod {
    @Subscribe
    public void onStartup(StartupEvent event) {
        // Automatically registered when mod loads
    }
    
    @Subscribe(phase = EventPhase.EARLY, receiveCancelled = true)
    public void earlyHandler(ShutdownEvent event) {
        // Runs early and receives cancelled events
    }
}
```

## Event Lifecycle

1. **Event Creation** - An event object is instantiated
2. **Event Dispatch** - `EventBus.fire()` is called
3. **Phase Execution** - Listeners are invoked in phase order (EARLY → DEFAULT → LATE)
4. **Cancellation Check** - If the event is cancellable and cancelled, `fire()` returns false
5. **Default Behavior** - The caller may skip default behavior if the event was cancelled

## Built-in Events

### Lifecycle Events

#### StartupEvent
Fired when the application starts up.

```java
@Subscribe
public void onStartup(StartupEvent event) {
    String[] args = event.getArgs();
    // Initialize mod
}
```

#### ShutdownEvent
Fired when the application is shutting down. This event is cancellable.

```java
@Subscribe
public void onShutdown(ShutdownEvent event) {
    if (event.getReason() == ShutdownReason.ERROR) {
        // Maybe cancel shutdown to save state?
        event.setCancelled(true);
    }
}
```

## Implementation Status (v26.0-Alpha.3)

### Completed
- ✅ Event interface hierarchy
- ✅ EventPhase enum
- ✅ EventListener functional interface
- ✅ @Subscribe annotation
- ✅ EventBus API definition
- ✅ Lifecycle event types (StartupEvent, ShutdownEvent)
- ✅ Comprehensive test suite

### Not Yet Implemented (Alpha.3)
- ⏳ EventBus implementation (agent-side)
- ⏳ @Subscribe annotation scanning
- ⏳ Actual event dispatching

The API is complete and tested. Implementation will be added in aprismate-agent in future releases.

## Design Principles

1. **Type Safety** - Events are strongly typed, catching errors at compile time
2. **Simplicity** - Simple events require minimal boilerplate
3. **Flexibility** - Phases and cancellation provide control when needed
4. **Performance** - Designed for low overhead in the hot path
5. **Modularity** - API in aprismate-api, implementation in aprismate-agent

## Future Enhancements (Post-v26.0)

- Priority ordering within phases
- Async event dispatching
- Event filtering and transformation
- Generic event data payloads
- Event inheritance and polymorphic dispatch
- Per-thread event buses
- Event statistics and profiling

## Usage Examples

### Simple Event Listener

```java
EventBus.register(StartupEvent.class, event -> {
    System.out.println("App started with args: " + 
        String.join(", ", event.getArgs()));
});
```

### Cancellable Event

```java
EventBus.register(ShutdownEvent.class, event -> {
    if (!saveState()) {
        event.setCancelled(true);
    }
}, EventPhase.EARLY);

ShutdownEvent event = new ShutdownEvent(ShutdownReason.NORMAL);
boolean shouldProceed = EventBus.fire(event);
if (shouldProceed) {
    performShutdown();
}
```

### Custom Event

```java
public class PlayerConnectEvent implements CancellableEvent {
    private final String playerName;
    private final InetAddress address;
    private boolean cancelled = false;
    
    public PlayerConnectEvent(String playerName, InetAddress address) {
        this.playerName = playerName;
        this.address = address;
    }
    
    public String getPlayerName() { return playerName; }
    public InetAddress getAddress() { return address; }
    
    @Override
    public boolean isCancelled() { return cancelled; }
    
    @Override
    public void setCancelled(boolean cancelled) { 
        this.cancelled = cancelled; 
    }
}

// Usage in server code
PlayerConnectEvent event = new PlayerConnectEvent(name, addr);
if (EventBus.fire(event)) {
    acceptConnection(name, addr);
} else {
    rejectConnection(name, addr);
}
```

### Annotation-Based Registration

```java
public class SecurityMod {
    @Subscribe(phase = EventPhase.EARLY)
    public void checkPlayerSecurity(PlayerConnectEvent event) {
        if (isBlacklisted(event.getAddress())) {
            event.setCancelled(true);
        }
    }
}
```

## Testing

Event types should be tested for:
- Proper initialization
- Immutability (where applicable)
- Cancellation behavior
- Defensive copying of mutable fields

See `EventTest`, `EventPhaseTest`, and `LifecycleEventTest` for examples.
