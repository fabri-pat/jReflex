package io.github.fabripat.jreflex.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If present, class (entity or dto only) will be excluded from automated bean testing.
 * Remember to provide custom test implementation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExcludeBeanTesting {
}