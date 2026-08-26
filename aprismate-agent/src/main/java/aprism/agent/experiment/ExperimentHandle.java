package aprism.agent.experiment;

import java.time.Instant;

/**
 * Handle for a live experiment: identifies what was changed and keeps
 * the original bytes for rollback.
 */
public final class ExperimentHandle {

    private final String className;
    private final Instant appliedAt;
    private final byte[] originalBytes;

    ExperimentHandle(String className, Instant appliedAt, byte[] originalBytes) {
        this.className = className;
        this.appliedAt = appliedAt;
        this.originalBytes = originalBytes;
    }

    public String className() { return className; }
    public Instant appliedAt() { return appliedAt; }
    byte[] originalBytes() { return originalBytes; }

    @Override
    public String toString() {
        return "Experiment[" + className + " @ " + appliedAt + "]";
    }
}
