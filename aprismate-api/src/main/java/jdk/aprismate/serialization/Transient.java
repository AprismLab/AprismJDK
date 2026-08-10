package jdk.aprismate.serialization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as transient for serialization.
 * <p>
 * Fields marked with this annotation will be skipped during serialization
 * and will retain their default values during deserialization.
 * </p>
 * <p>
 * This is similar to Java's transient keyword but works with AprismJDK's
 * serialization system and can be used on non-primitive fields.
 * </p>
 *
 * @since 26.0-Alpha.8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Transient {
}
