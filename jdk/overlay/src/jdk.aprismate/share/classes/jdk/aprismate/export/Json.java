package jdk.aprismate.export;

/**
 * Minimal JSON string builder -- no external dependencies.
 * Handles escaping, nesting, and all JSON value types.
 */
public final class Json {

    private final StringBuilder sb = new StringBuilder(4096);
    private final boolean pretty;
    private int depth;
    private boolean needsComma;

    public Json() {
        this(false);
    }

    public Json(boolean pretty) {
        this.pretty = pretty;
    }

    public Json startObject() {
        preValue();
        sb.append('{');
        depth++;
        needsComma = false;
        return this;
    }

    public Json endObject() {
        depth--;
        if (needsComma && pretty) {
            newline();
        }
        sb.append('}');
        needsComma = true;
        return this;
    }

    public Json startArray() {
        preValue();
        sb.append('[');
        depth++;
        needsComma = false;
        return this;
    }

    public Json endArray() {
        depth--;
        if (needsComma && pretty) {
            newline();
        }
        sb.append(']');
        needsComma = true;
        return this;
    }

    public Json key(String name) {
        comma();
        indent();
        sb.append(quote(name)).append(':');
        if (pretty) {
            sb.append(' ');
        }
        needsComma = false;
        return this;
    }

    public Json value(String v) {
        preValue();
        sb.append(v == null ? "null" : quote(v));
        needsComma = true;
        return this;
    }

    public Json value(long v) {
        preValue();
        sb.append(v);
        needsComma = true;
        return this;
    }

    public Json value(double v) {
        preValue();
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            sb.append("null");
        } else {
            sb.append(v);
        }
        needsComma = true;
        return this;
    }

    public Json value(boolean v) {
        preValue();
        sb.append(v);
        needsComma = true;
        return this;
    }

    public Json nullValue() {
        preValue();
        sb.append("null");
        needsComma = true;
        return this;
    }

    /** Raw JSON fragment -- caller is responsible for validity. */
    public Json raw(String json) {
        preValue();
        sb.append(json);
        needsComma = true;
        return this;
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    private void preValue() {
        comma();
        if (!sb.isEmpty() && sb.charAt(sb.length() - 1) != '{' && sb.charAt(sb.length() - 1) != '['
                && sb.charAt(sb.length() - 1) != ':' && sb.charAt(sb.length() - 1) != ',') {
            // mid-array value needs indentation only in pretty mode
        }
    }

    private void comma() {
        if (needsComma) {
            sb.append(',');
        }
    }

    private void indent() {
        if (pretty) {
            newline();
            sb.append("  ".repeat(Math.max(0, depth)));
        }
    }

    private void newline() {
        sb.append('\n');
    }

    static String quote(String s) {
        var out = new StringBuilder(s.length() + 8);
        out.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        out.append('"');
        return out.toString();
    }
}
