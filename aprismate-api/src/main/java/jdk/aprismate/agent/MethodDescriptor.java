package jdk.aprismate.agent;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;

/**
 * Describes a method to be added to a class during structural redefinition.
 * <p>
 * This descriptor contains all information needed to add a new method to an existing class:
 * method name, return type, parameter types, modifiers, and bytecode.
 * </p>
 * 
 * @since 26.1
 */
public final class MethodDescriptor {
    private final String name;
    private final Class<?> returnType;
    private final Class<?>[] parameterTypes;
    private final int modifiers;
    private final byte[] bytecode;
    private final Class<?>[] exceptionTypes;
    
    private MethodDescriptor(String name, Class<?> returnType, Class<?>[] parameterTypes,
                            int modifiers, byte[] bytecode, Class<?>[] exceptionTypes) {
        this.name = Objects.requireNonNull(name, "Method name cannot be null");
        this.returnType = Objects.requireNonNull(returnType, "Return type cannot be null");
        this.parameterTypes = parameterTypes != null ? parameterTypes.clone() : new Class<?>[0];
        this.modifiers = modifiers;
        this.bytecode = bytecode != null ? bytecode.clone() : null;
        this.exceptionTypes = exceptionTypes != null ? exceptionTypes.clone() : new Class<?>[0];
        
        validateMethodName(name);
        validateParameterTypes(this.parameterTypes);
        validateExceptionTypes(this.exceptionTypes);
    }
    
    /**
     * Returns the method name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the return type.
     */
    public Class<?> getReturnType() {
        return returnType;
    }
    
    /**
     * Returns the parameter types.
     */
    public Class<?>[] getParameterTypes() {
        return parameterTypes.clone();
    }
    
    /**
     * Returns the method modifiers (public, private, static, final, etc.).
     */
    public int getModifiers() {
        return modifiers;
    }
    
    /**
     * Returns the method bytecode, or null if not specified.
     */
    public byte[] getBytecode() {
        return bytecode != null ? bytecode.clone() : null;
    }
    
    /**
     * Returns the exception types declared by this method.
     */
    public Class<?>[] getExceptionTypes() {
        return exceptionTypes.clone();
    }
    
    /**
     * Returns true if this is a static method.
     */
    public boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }
    
    /**
     * Returns true if this is a final method.
     */
    public boolean isFinal() {
        return Modifier.isFinal(modifiers);
    }
    
    /**
     * Returns true if this is a public method.
     */
    public boolean isPublic() {
        return Modifier.isPublic(modifiers);
    }
    
    /**
     * Returns true if this is a private method.
     */
    public boolean isPrivate() {
        return Modifier.isPrivate(modifiers);
    }
    
    /**
     * Returns true if this is a protected method.
     */
    public boolean isProtected() {
        return Modifier.isProtected(modifiers);
    }
    
    /**
     * Returns true if this is an abstract method.
     */
    public boolean isAbstract() {
        return Modifier.isAbstract(modifiers);
    }
    
    /**
     * Returns true if this is a native method.
     */
    public boolean isNative() {
        return Modifier.isNative(modifiers);
    }
    
    /**
     * Returns true if this is a synchronized method.
     */
    public boolean isSynchronized() {
        return Modifier.isSynchronized(modifiers);
    }
    
    /**
     * Returns the method signature in descriptor format (e.g., "(ILjava/lang/String;)V").
     */
    public String getDescriptor() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (Class<?> param : parameterTypes) {
            sb.append(getTypeDescriptor(param));
        }
        sb.append(')');
        sb.append(getTypeDescriptor(returnType));
        return sb.toString();
    }
    
    private String getTypeDescriptor(Class<?> type) {
        if (type == void.class) return "V";
        if (type == boolean.class) return "Z";
        if (type == byte.class) return "B";
        if (type == char.class) return "C";
        if (type == short.class) return "S";
        if (type == int.class) return "I";
        if (type == long.class) return "J";
        if (type == float.class) return "F";
        if (type == double.class) return "D";
        if (type.isArray()) {
            return "[" + getTypeDescriptor(type.getComponentType());
        }
        return "L" + type.getName().replace('.', '/') + ";";
    }
    
    private void validateMethodName(String name) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Method name cannot be empty");
        }
        if (!name.equals("<init>") && !name.equals("<clinit>")) {
            if (!Character.isJavaIdentifierStart(name.charAt(0))) {
                throw new IllegalArgumentException("Invalid method name: " + name);
            }
            for (int i = 1; i < name.length(); i++) {
                if (!Character.isJavaIdentifierPart(name.charAt(i))) {
                    throw new IllegalArgumentException("Invalid method name: " + name);
                }
            }
        }
    }
    
    private void validateParameterTypes(Class<?>[] types) {
        for (int i = 0; i < types.length; i++) {
            if (types[i] == null) {
                throw new IllegalArgumentException("Parameter type at index " + i + " is null");
            }
        }
    }
    
    private void validateExceptionTypes(Class<?>[] types) {
        for (int i = 0; i < types.length; i++) {
            if (types[i] == null) {
                throw new IllegalArgumentException("Exception type at index " + i + " is null");
            }
            if (!Throwable.class.isAssignableFrom(types[i])) {
                throw new IllegalArgumentException(
                    "Exception type must extend Throwable: " + types[i].getName());
            }
        }
    }
    
    @Override
    public String toString() {
        return "MethodDescriptor{" +
               "name='" + name + '\'' +
               ", returnType=" + returnType.getName() +
               ", parameterTypes=" + Arrays.toString(parameterTypes) +
               ", modifiers=" + Modifier.toString(modifiers) +
               ", bytecode=" + (bytecode != null ? bytecode.length + " bytes" : "null") +
               ", exceptionTypes=" + Arrays.toString(exceptionTypes) +
               '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodDescriptor)) return false;
        MethodDescriptor that = (MethodDescriptor) o;
        return modifiers == that.modifiers &&
               name.equals(that.name) &&
               returnType.equals(that.returnType) &&
               Arrays.equals(parameterTypes, that.parameterTypes) &&
               Arrays.equals(bytecode, that.bytecode) &&
               Arrays.equals(exceptionTypes, that.exceptionTypes);
    }
    
    @Override
    public int hashCode() {
        int result = Objects.hash(name, returnType, modifiers);
        result = 31 * result + Arrays.hashCode(parameterTypes);
        result = 31 * result + Arrays.hashCode(bytecode);
        result = 31 * result + Arrays.hashCode(exceptionTypes);
        return result;
    }
    
    /**
     * Creates a new builder for constructing a MethodDescriptor.
     */
    public static Builder builder(String name, Class<?> returnType) {
        if (name == null) {
            throw new NullPointerException("Method name cannot be null");
        }
        if (returnType == null) {
            throw new NullPointerException("Return type cannot be null");
        }
        return new Builder(name, returnType);
    }
    
    /**
     * Builder for creating MethodDescriptor instances.
     */
    public static final class Builder {
        private final String name;
        private final Class<?> returnType;
        private Class<?>[] parameterTypes = new Class<?>[0];
        private int modifiers = 0;
        private byte[] bytecode = null;
        private Class<?>[] exceptionTypes = new Class<?>[0];
        
        private Builder(String name, Class<?> returnType) {
            this.name = name;
            this.returnType = returnType;
        }
        
        /**
         * Sets the parameter types.
         */
        public Builder parameterTypes(Class<?>... types) {
            this.parameterTypes = types != null ? types : new Class<?>[0];
            return this;
        }
        
        /**
         * Sets the method modifiers.
         */
        public Builder modifiers(int modifiers) {
            this.modifiers = modifiers;
            return this;
        }
        
        /**
         * Makes the method public.
         */
        public Builder makePublic() {
            this.modifiers |= Modifier.PUBLIC;
            return this;
        }
        
        /**
         * Makes the method private.
         */
        public Builder makePrivate() {
            this.modifiers |= Modifier.PRIVATE;
            return this;
        }
        
        /**
         * Makes the method protected.
         */
        public Builder makeProtected() {
            this.modifiers |= Modifier.PROTECTED;
            return this;
        }
        
        /**
         * Makes the method static.
         */
        public Builder makeStatic() {
            this.modifiers |= Modifier.STATIC;
            return this;
        }
        
        /**
         * Makes the method final.
         */
        public Builder makeFinal() {
            this.modifiers |= Modifier.FINAL;
            return this;
        }
        
        /**
         * Makes the method abstract.
         */
        public Builder makeAbstract() {
            this.modifiers |= Modifier.ABSTRACT;
            return this;
        }
        
        /**
         * Makes the method native.
         */
        public Builder makeNative() {
            this.modifiers |= Modifier.NATIVE;
            return this;
        }
        
        /**
         * Makes the method synchronized.
         */
        public Builder makeSynchronized() {
            this.modifiers |= Modifier.SYNCHRONIZED;
            return this;
        }
        
        /**
         * Sets the method bytecode.
         */
        public Builder bytecode(byte[] bytecode) {
            this.bytecode = bytecode;
            return this;
        }
        
        /**
         * Sets the exception types declared by this method.
         */
        public Builder exceptionTypes(Class<?>... types) {
            this.exceptionTypes = types != null ? types : new Class<?>[0];
            return this;
        }
        
        /**
         * Builds the MethodDescriptor.
         */
        public MethodDescriptor build() {
            return new MethodDescriptor(name, returnType, parameterTypes, modifiers, bytecode, exceptionTypes);
        }
    }
}
