package io.github.fabripat.jreflex.annotations;

import java.lang.annotation.*;

/**
 * <p>
 * This annotation is used to specify the source root path of the project
 * and the packages you want to scan for automated bean testing.
 * </p>
 * <p>
 * This annotatation must be present in the class that extends @{@link io.github.fabripat.jreflex.AbstractReflectionBean}.<br>
 * </p>
 * <p>
 * To use this annotation, specify the root path of the project using the {@code sourceRootPath} attribute.<br>
 * Example: "io.github.fabripat.jreflex"
 * <p>
 * If this attribute will not be specified the system will calculate the source root path of the test class.
 * </p>
 * <p>
 * You can also specify individual sub-packages to scan for beans using the {@code packagesToScan} attribute.<br>
 * If this attribute will not be specified the system will scan all sub-packages get by {@code sourceRootPath} attribute.
 * </p>
 *
 * @author Fabrizio Patruno
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableAutomatedBeanTesting {

    /**
     * The root path of the project, used to identify the root package to scan for beans.<br>
     * Beans within this path and subpaths will be automatically scanned and tested.
     *
     * @return The root path of the project.
     */
    String sourceRootPath();

    /**
     * <p>
     * The individual packages to scan for beans.
     * </p>
     * <p>
     * Beans within these packages will be automatically scanned and tested.
     * </p>
     *
     * @return The packages to scan for beans.
     */
    String[] packagesToScan() default {};
}
