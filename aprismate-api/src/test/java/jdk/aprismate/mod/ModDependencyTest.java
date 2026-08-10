package jdk.aprismate.mod;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ModDependency interface.
 */
class ModDependencyTest {
    
    @Test
    void dependencyTypeShouldHaveFiveTypes() {
        ModDependency.DependencyType[] types = ModDependency.DependencyType.values();
        assertEquals(5, types.length, "Should have exactly 5 dependency types");
    }
    
    @Test
    void dependencyTypeShouldHaveRequired() {
        ModDependency.DependencyType type = ModDependency.DependencyType.valueOf("REQUIRED");
        assertNotNull(type);
        assertEquals(ModDependency.DependencyType.REQUIRED, type);
    }
    
    @Test
    void dependencyTypeShouldHaveOptional() {
        ModDependency.DependencyType type = ModDependency.DependencyType.valueOf("OPTIONAL");
        assertNotNull(type);
        assertEquals(ModDependency.DependencyType.OPTIONAL, type);
    }
    
    @Test
    void dependencyTypeShouldHaveConflicts() {
        ModDependency.DependencyType type = ModDependency.DependencyType.valueOf("CONFLICTS");
        assertNotNull(type);
        assertEquals(ModDependency.DependencyType.CONFLICTS, type);
    }
    
    @Test
    void dependencyTypeShouldHaveBefore() {
        ModDependency.DependencyType type = ModDependency.DependencyType.valueOf("BEFORE");
        assertNotNull(type);
        assertEquals(ModDependency.DependencyType.BEFORE, type);
    }
    
    @Test
    void dependencyTypeShouldHaveAfter() {
        ModDependency.DependencyType type = ModDependency.DependencyType.valueOf("AFTER");
        assertNotNull(type);
        assertEquals(ModDependency.DependencyType.AFTER, type);
    }
    
    @Test
    void testDependencyImplementation() {
        TestModDependency dep = new TestModDependency(
            "example-mod", 
            "[1.0,2.0)", 
            true, 
            ModDependency.DependencyType.REQUIRED
        );
        
        assertEquals("example-mod", dep.getModId());
        assertEquals("[1.0,2.0)", dep.getVersionRange());
        assertTrue(dep.isRequired());
        assertEquals(ModDependency.DependencyType.REQUIRED, dep.getType());
    }
    
    @Test
    void testOptionalDependency() {
        TestModDependency dep = new TestModDependency(
            "optional-mod", 
            "*", 
            false, 
            ModDependency.DependencyType.OPTIONAL
        );
        
        assertFalse(dep.isRequired());
        assertEquals(ModDependency.DependencyType.OPTIONAL, dep.getType());
    }
    
    // Test implementation of ModDependency
    private static class TestModDependency implements ModDependency {
        private final String modId;
        private final String versionRange;
        private final boolean required;
        private final DependencyType type;
        
        TestModDependency(String modId, String versionRange, boolean required, DependencyType type) {
            this.modId = modId;
            this.versionRange = versionRange;
            this.required = required;
            this.type = type;
        }
        
        @Override
        public String getModId() {
            return modId;
        }
        
        @Override
        public String getVersionRange() {
            return versionRange;
        }
        
        @Override
        public boolean isRequired() {
            return required;
        }
        
        @Override
        public DependencyType getType() {
            return type;
        }
    }
}
