package aprism.agent.reload;

import java.util.List;
import java.util.Map;

/**
 * A proposed set of class replacements (the "change set") that can be
 * applied atomically via retransformClasses. Built by diffing original
 * vs proposed bytecode for each class.
 */
public final class ChangeSet {

    private final String id;
    private final Map<String, byte[]> replacements;
    private final long createdAt;

    ChangeSet(String id, Map<String, byte[]> replacements) {
        this.id = id;
        this.replacements = Map.copyOf(replacements);
        this.createdAt = System.nanoTime();
    }

    public String id() { return id; }
    public long createdAt() { return createdAt; }
    public int size() { return replacements.size(); }

    /** Class names (dotted) in this change set. */
    public List<String> classNames() {
        return List.copyOf(replacements.keySet());
    }

    /** New bytecode for a class, or null if not in this set. */
    public byte[] replacementFor(String dottedClassName) {
        return replacements.get(dottedClassName);
    }

    @Override
    public String toString() {
        return "ChangeSet[" + id + ": " + replacements.size() + " classes]";
    }
}
