package jdk.aprismate.agent;

import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * Describes a field to be added to a class during structural redefinition.
 * <p>
 * This descriptor contains all information needed to add a new field to an existing class:
 * field name, type, modifiers, and optional initial value.
 * </p>
 * 
 * @since 26.1
 */
public final class FieldDescriptor {
    private final String name;
    private final Class<?> type;
    private final int modifiers;
    private final Object initialValue;
    
    private FieldDescriptor(String name, Class<?> type, int modifiers, Object initialValue) {
        this.name = Objects.requireNonNull(name, "Field name cannot be null");
        this.type = Objects.requireNonNull(type, "Field type cannot be null");
        this.modifiers = modifiers;
        this.initialValue = initialValue;
        
        validateFieldName(name);
        validateInitialValue(type, initialValue);
    }
    
    /**
     * Returns the field name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the field type.
     */
    public Class<?> getType() {
        return type;
    }
    
    /**
     * Returns the field modifiers (public, private, static, final, etc.).
     */
    public int getModifiers() {
        return modifiers;
    }
    
    /**
     * Returns the initial value for the field, or null if none specified.
     */
    public Object getInitialValue() {
        return initialValue;
    }
    
    /**
     * Returns true if this is a static field.
     */
    public boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }
    
    /**
     * Returns true if this is a final field.
     */
    public boolean isFinal() {
        return Modifier.isFinal(modifiers);
    }
    
    /**
     * Returns true if this is a public field.
     */
    public boolean isPublic() {
        return Modifier.isPublic(modifiers);
    }
    
    /**
     * Returns true if this is a private field.
     */
    public boolean isPrivate() {
        return Modifier.isPrivate(modifiers);
    }
    
    /**
     * Returns true if this is a protected field.
     */
    public boolean isProtected() {
        return Modifier.isProtected(modifiers);
    }
    
    private void validateFieldName(String name) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Field name cannot be empty");
        }
        if (!Character.isJavaIdentifierStart(name.charAt(0))) {
            throw new IllegalArgumentException("Invalid field name: " + name);
        }
        for (int i = 1; i < name.length(); i++) {
            if (!Character.isJavaIdentifierPart(name.charAt(i))) {
                throw new IllegalArgumentException("Invalid field name: " + name);
            }
        }
    }
    
    private void validateInitialValue(Class<?> type, Object value) {
        if (value == null) {
            return; // null is valid for reference types and will use default values for primitives
        }
        
        // Check type compatibility
        if (type.isPrimitive()) {
            Class<?> wrapper = getWrapperClass(type);
            if (!wrapper.isInstance(value)) {
                throw new IllegalArgumentException(
                    "Initial value type mismatch: expected " + type + ", got " + value.getClass());
            }
        } else {
            if (!type.isInstance(value)) {
                throw new IllegalArgumentException(
                    "Initial value type mismatch: expected " + type + ", got " + value.getClass());
            }
        }
    }
    
    private Class<?> getWrapperClass(Class<?> primitiveType) {
        if (primitiveType == int.class) return Integer.class;
        if (primitiveType == long.class) return Long.class;
        if (primitiveType == double.class) return Double.class;
        if (primitiveType == float.class) return Float.class;
        if (primitiveType == boolean.class) return Boolean.class;
        if (primitiveType == byte.class) return Byte.class;
        if (primitiveType == short.class) return Short.class;
        if (primitiveType == char.class) return Character.class;
        throw new IllegalArgumentException("Not a primitive type: " + primitiveType);
    }
    
    @Override
    public String toString() {
        return "FieldDescriptor{" +
               "name='" + name + '\'' +
               ", type=" + type.getName() +
               ", modifiers=" + Modifier.toString(modifiers) +
               ", initialValue=" + initialValue +
               '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FieldDescriptor)) return false;
        FieldDescriptor that = (FieldDescriptor) o;
        return modifiers == that.modifiers &&
               name.equals(that.name) &&
               type.equals(that.type) &&
               Objects.equals(initialValue, that.initialValue);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, type, modifiers, initialValue);
    }
    
    /**
     * Creates a new builder for constructing a FieldDescriptor.
     */
    public static Builder builder(String name, Class<?> type) {
        if (name == null) {
            throw new NullPointerException("Field name cannot be null");
        }
        if (type == null) {
            throw new NullPointerException("Field type cannot be null");
        }
        return new Builder(name, type);
    }
    
    /**
     * Builder for creating FieldDescriptor instances.
     */
    public static final class Builder {
        private final String name;
        private final Class<?> type;
        private int modifiers = 0;
        private Object initialValue = null;
        
        private Builder(String name, Class<?> type) {
            this.name = name;
            this.type = type;
        }
        
        /**
         * Sets the field modifiers.
         */
        public Builder modifiers(int modifiers) {
            this.modifiers = modifiers;
            return this;
        }
        
        /**
         * Makes the field public.
         */
        public Builder makePublic() {
            this.modifiers |= Modifier.PUBLIC;
            return this;
        }
        
        /**
         * Makes the field private.
         */
        public Builder makePrivate() {
            this.modifiers |= Modifier.PRIVATE;
            return this;
        }
        
        /**
         * Makes the field protected.
         */
        public Builder makeProtected() {
            this.modifiers |= Modifier.PROTECTED;
            return this;
        }
        
        /**
         * Makes the field static.
         */
        public Builder makeStatic() {
            this.modifiers |= Modifier.STATIC;
            return this;
        }
        
        /**
         * Makes the field final.
         */
        public Builder makeFinal() {
            this.modifiers |= Modifier.FINAL;
            return this;
        }
        
        /**
         * Makes the field volatile.
         */
        public Builder makeVolatile() {
            this.modifiers |= Modifier.VOLATILE;
            return this;
        }
        
        /**
         * Makes the field transient.
         */
        public Builder makeTransient() {
            this.modifiers |= Modifier.TRANSIENT;
            return this;
        }
        
        /**
         * Sets the initial value for the field.
         */
        public Builder initialValue(Object value) {
            this.initialValue = value;
            return this;
        }
        
        /**
         * Builds the FieldDescriptor.
         */
        public FieldDescriptor build() {
            return new FieldDescriptor(name, type, modifiers, initialValue);
        }
    }
}
