package io.github.fabripat.jreflex.annotations;

import java.lang.annotation.*;

/**
 * <p>
 * The annotated class with this annotation will be excluded from automated bean testing.
 * </p>
 * <p>
 * Remember to provide custom test implementation.
 * </p>
 * @author Fabrizio Patruno
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExcludeBeanTesting {
}