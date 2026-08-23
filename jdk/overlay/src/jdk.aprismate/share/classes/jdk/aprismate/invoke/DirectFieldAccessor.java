package jdk.aprismate.invoke;

import java.lang.reflect.Field;

/**
 * A cached, low-overhead field read/write pair bound to one field.
 */
public interface DirectFieldAccessor {

    Object get(Object target) throws Throwable;

    void set(Object target, Object value) throws Throwable;

    Field field();

    Strategy strategy();
}
