package jdk.aprismate.network;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for NetworkSide enum.
 */
class NetworkSideTest {
    
    @Test
    void shouldHaveClientSide() {
        assertNotNull(NetworkSide.CLIENT);
    }
    
    @Test
    void shouldHaveServerSide() {
        assertNotNull(NetworkSide.SERVER);
    }
    
    @Test
    void shouldHaveTwoSides() {
        NetworkSide[] sides = NetworkSide.values();
        assertEquals(2, sides.length);
    }
    
    @Test
    void oppositeShouldReturnOtherSide() {
        assertEquals(NetworkSide.SERVER, NetworkSide.CLIENT.opposite());
        assertEquals(NetworkSide.CLIENT, NetworkSide.SERVER.opposite());
    }
    
    @Test
    void isClientShouldReturnTrueForClient() {
        assertTrue(NetworkSide.CLIENT.isClient());
        assertFalse(NetworkSide.SERVER.isClient());
    }
    
    @Test
    void isServerShouldReturnTrueForServer() {
        assertTrue(NetworkSide.SERVER.isServer());
        assertFalse(NetworkSide.CLIENT.isServer());
    }
    
    @Test
    void valueOfShouldWork() {
        assertEquals(NetworkSide.CLIENT, NetworkSide.valueOf("CLIENT"));
        assertEquals(NetworkSide.SERVER, NetworkSide.valueOf("SERVER"));
    }
}
