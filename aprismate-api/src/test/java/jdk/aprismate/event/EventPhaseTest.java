package jdk.aprismate.event;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for EventPhase enum.
 */
class EventPhaseTest {
    
    @Test
    void shouldHaveThreePhases() {
        EventPhase[] phases = EventPhase.values();
        assertEquals(3, phases.length, "Should have exactly 3 phases");
    }
    
    @Test
    void shouldHaveEarlyPhase() {
        EventPhase phase = EventPhase.valueOf("EARLY");
        assertNotNull(phase, "EARLY phase should exist");
        assertEquals(EventPhase.EARLY, phase);
    }
    
    @Test
    void shouldHaveDefaultPhase() {
        EventPhase phase = EventPhase.valueOf("DEFAULT");
        assertNotNull(phase, "DEFAULT phase should exist");
        assertEquals(EventPhase.DEFAULT, phase);
    }
    
    @Test
    void shouldHaveLatePhase() {
        EventPhase phase = EventPhase.valueOf("LATE");
        assertNotNull(phase, "LATE phase should exist");
        assertEquals(EventPhase.LATE, phase);
    }
    
    @Test
    void getDefaultShouldReturnDefaultPhase() {
        assertEquals(EventPhase.DEFAULT, EventPhase.getDefault(), 
            "getDefault() should return DEFAULT phase");
    }
    
    @Test
    void phaseOrderShouldBeCorrect() {
        EventPhase[] phases = EventPhase.values();
        assertEquals(EventPhase.EARLY, phases[0], "First phase should be EARLY");
        assertEquals(EventPhase.DEFAULT, phases[1], "Second phase should be DEFAULT");
        assertEquals(EventPhase.LATE, phases[2], "Third phase should be LATE");
    }
}
