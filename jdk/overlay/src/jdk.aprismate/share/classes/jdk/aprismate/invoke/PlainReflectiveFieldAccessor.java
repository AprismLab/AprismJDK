package jdk.aprismate.invoke;

import java.lang.reflect.Field;

/**
 * Baseline field bridge used when MethodHandle construction is
 * impossible.
 */
final class PlainReflectiveFieldAccessor implements DirectFieldAccessor {

    private final Field field;

    PlainReflectiveFieldAccessor(Field field) {
        this.field = field;
    }

    @Override
    public Object get(Object target) throws Throwable {
        return field.get(target);
    }

    @Override
    public void set(Object target, Object value) throws Throwable {
        field.set(target, value);
    }

    @Override
    public Field field() {
        return field;
    }

    @Override
    public Strategy strategy() {
        return Strategy.PLAIN_REFLECTIVE;
    }
}
