package io.github.fabripat.jreflex;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * If present exclude annotated field from automatic bean testing.
 * Pay attention, use this annotation in combination with ToString.Exclude and
 * EqualsAndHashCode.Exclude (Lombok annotations) otherwise test will fail.
 * Please consider the usage of {@link ExcludeBeanTesting} to exclude automatic testing
 * and use custom testing implementation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExcludeFieldBeanTesting {
}