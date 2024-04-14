package io.github.fabripat.jreflex.annotations;

import java.lang.annotation.*;

/**
 * <p>
 * Exclude annotated field from automatic bean testing.
 * </p>
 * <p>
 * Pay attention, use this annotation if you are actually excluding the annotated field from
 * {@code toString(), equals(), hashCode()} method, otherwise test will fail!<br>
 * Please consider the usage of {@link ExcludeBeanTesting} to exclude automatic testing
 * and use custom testing implementation.
 * </p>
 * @author Fabrizio Patruno
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExcludeFieldBeanTesting {
}