package jdk.aprismate.event;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Event interface and basic event functionality.
 */
class EventTest {
    
    private static class SimpleEvent implements Event {
    }
    
    private static class SimpleCancellableEvent implements CancellableEvent {
        private boolean cancelled = false;
        
        @Override
        public boolean isCancelled() {
            return cancelled;
        }
        
        @Override
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }
    
    @Test
    void simpleEventShouldNotBeCancellable() {
        Event event = new SimpleEvent();
        assertFalse(event.isCancellable(), "Simple events should not be cancellable");
    }
    
    @Test
    void cancellableEventShouldBeCancellable() {
        Event event = new SimpleCancellableEvent();
        assertTrue(event.isCancellable(), "Cancellable events should be cancellable");
    }
    
    @Test
    void cancellableEventShouldStartNotCancelled() {
        SimpleCancellableEvent event = new SimpleCancellableEvent();
        assertFalse(event.isCancelled(), "Cancellable events should start not cancelled");
    }
    
    @Test
    void cancellableEventCanBeCancelled() {
        SimpleCancellableEvent event = new SimpleCancellableEvent();
        event.setCancelled(true);
        assertTrue(event.isCancelled(), "Event should be cancelled after setCancelled(true)");
    }
    
    @Test
    void cancellableEventCanBeUncancelled() {
        SimpleCancellableEvent event = new SimpleCancellableEvent();
        event.setCancelled(true);
        event.setCancelled(false);
        assertFalse(event.isCancelled(), "Event should not be cancelled after setCancelled(false)");
    }
    
    @Test
    void eventShouldHaveDefaultPhase() {
        Event event = new SimpleEvent();
        assertEquals(EventPhase.DEFAULT, event.getPhase(), "Events should have DEFAULT phase by default");
    }
}
