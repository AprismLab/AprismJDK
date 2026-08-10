package jdk.aprismate.serialization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a custom name for a field during serialization.
 * <p>
 * By default, serializers use the field's declared name. This annotation
 * allows specifying a different name for the serialized form, which is
 * useful for:
 * </p>
 * <ul>
 *   <li>Maintaining backwards compatibility when renaming fields</li>
 *   <li>Using different naming conventions (camelCase vs snake_case)</li>
 *   <li>Shortening field names to reduce serialized size</li>
 * </ul>
 *
 * @since 26.0-Alpha.8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SerializedName {
    
    /**
     * The name to use in the serialized form.
     *
     * @return the serialized name
     */
    String value();
}
