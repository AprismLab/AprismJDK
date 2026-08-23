package jdk.aprismate.jit;

import java.lang.reflect.Method;

/**
 * DeoptimizationListener - Listener for method deoptimization events.
 * 
 * <p>Called when a compiled method is deoptimized and reverted to
 * interpreter mode. This typically happens when:
 * <ul>
 *   <li>Speculative optimizations were invalidated</li>
 *   <li>Class hierarchy changed (new subclass loaded)</li>
 *   <li>Uncommon trap triggered</li>
 *   <li>Method became not entrant (replaced by newer version)</li>
 * </ul>
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
@FunctionalInterface
public interface DeoptimizationListener {
    
    /**
     * Called when a method is deoptimized.
     * 
     * @param method the deoptimized method
     * @param reason the deoptimization reason
     */
    void onDeoptimization(Method method, DeoptimizationReason reason);
    
    /**
     * Deoptimization reason enumeration.
     */
    enum DeoptimizationReason {
        /** Uncommon trap triggered. */
        UNCOMMON_TRAP("uncommon trap"),
        
        /** Method became not entrant (replaced). */
        NOT_ENTRANT("not entrant"),
        
        /** Class hierarchy changed. */
        CLASS_HIERARCHY_CHANGED("class hierarchy changed"),
        
        /** Type speculation failed. */
        TYPE_SPECULATION_FAILED("type speculation failed"),
        
        /** Null check elimination failed. */
        NULL_CHECK_FAILED("null check failed"),
        
        /** Range check elimination failed. */
        RANGE_CHECK_FAILED("range check failed"),
        
        /** Division by zero. */
        DIV_BY_ZERO("division by zero"),
        
        /** Other reason. */
        OTHER("other");
        
        private final String description;
        
        DeoptimizationReason(String description) {
            this.description = description;
        }
        
        /**
         * Returns the description.
         * 
         * @return description string
         */
        public String description() {
            return description;
        }
        
        @Override
        public String toString() {
            return description;
        }
    }
}
