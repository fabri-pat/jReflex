package io.github.fabripat.jreflex.provider;


import io.github.fabripat.jreflex.annotations.EnableAutomatedBeanTesting;
import io.github.fabripat.jreflex.annotations.ExcludeBeanTesting;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.platform.commons.PreconditionViolationException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * This class implements the ArgumentsProvider interface of JUnit Jupiter.
 * It provides arguments for parameterized tests but is designed for internal
 * functionality and is not intended to be used directly outside the package.
 * <p>
 * The class does not contain external functionalities exposed to the user.
 * It internally handles the automatic retrieval of JavaBeans classes to test.
 * <p>
 * Users should not extend or directly use this class, but rather utilize
 * its functionalities through the JUnit Jupiter APIs.
 */
public class ClassTestProvider implements ArgumentsProvider {

    private static final Logger log = Logger.getLogger(ClassTestProvider.class.getName());

    /**
     * <p>
     * This method extracts a specified annotation from the provided test class and composes
     * the paths of its attributes. The attributes are expected to represent package names
     * that need to be scanned for tests. The method returns a list of package paths to be
     * scanned.
     * </p>
     * The method serves as a utility for dynamically determining the package paths to be
     * scanned based on annotations present in the test class.
     *
     * @param context the test execution context
     * @return the list of packages to scan
     */
    private static List<String> getPackagesToScanFromSourcePath(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();

        EnableAutomatedBeanTesting enableAutomatedBeanTestingAnnotation =
                getEnableAutomatedBeanTestingAnnotationFromBeanTestClass(testClass);

        return getPathsFromEnableAutomatedBeanTestingAnnotation(enableAutomatedBeanTestingAnnotation);
    }

    /**
     * The method serves as a utility for dynamically determining the package paths to be
     * scanned based on annotations present in the test class.
     *
     * @param annotation the test class annotation
     * @return the list of packages to scan
     */
    private static List<String> getPathsFromEnableAutomatedBeanTestingAnnotation(EnableAutomatedBeanTesting annotation) {
        String rootPathPackageToScan = annotation.sourceRootPath()
                .replace(".", "/");

        String[] packagesToScan = annotation.packagesToScan();

        if (Arrays.asList(packagesToScan).isEmpty())
            return List.of(rootPathPackageToScan);

        return Arrays.stream(packagesToScan)
                .map(p -> rootPathPackageToScan.concat("/").concat(p))
                .collect(Collectors.toList());
    }

    /**
     * <p>
     * This method takes from a test class provided as input and extracts the 'EnableAutomatedBeanTesting' annotation,
     * if present. If the annotation is found, it is returned. If the annotation is not present in the
     * test class, a PreconditionViolationException is thrown, indicating that the annotation is required
     * but missing.
     * </p>
     * <p>
     * This method serves as a utility for ensuring that the 'EnableAutomatedBeanTesting' annotation is
     * properly applied to test classes before proceeding with automated bean testing functionality.
     * </p>
     *
     * @param testClass the test class from which to extract the 'EnableAutomatedBeanTesting' annotation
     * @return the 'EnableAutomatedBeanTesting' annotation if present in the test class
     * @throws PreconditionViolationException if the 'EnableAutomatedBeanTesting' annotation is not present
     *                                        in the test class
     */
    private static EnableAutomatedBeanTesting getEnableAutomatedBeanTestingAnnotationFromBeanTestClass(Class<?> testClass) {
        EnableAutomatedBeanTesting enableAutomatedBeanTestingAnnotation = testClass.getAnnotation(EnableAutomatedBeanTesting.class);

        if (enableAutomatedBeanTestingAnnotation == null) {
            throw new PreconditionViolationException(
                    String.format(
                            "{ %s } class must be annotated with { %s } annotation. " +
                                    "Please provide it or disable the test.",
                            testClass.getName(),
                            EnableAutomatedBeanTesting.class.getName()
                    )
            );
        }
        return enableAutomatedBeanTestingAnnotation;
    }

    /**
     * <p>
     * This method takes a list of strings identifying project resources and a string indicating
     * the root package, and composes a set of subpackages based on the provided resources and root
     * package. It returns the set of subpackages as a string.
     * </p>
     * <p>
     * The method serves as a utility for dynamically composing subpackages based on project resources
     * and a specified root package. It is particularly useful for organizing resources and packages
     * within a project structure.
     * </p>
     *
     * @param lines       a list of strings identifying project resources
     * @param rootPackage a string indicating the root package
     * @return a set of strings representing the set of subpackages derived from the project resources
     */
    private static Set<String> getSubPackagesFromResourcesAsList(final List<String> lines, final String rootPackage) {
        return lines.stream()
                .filter(line -> !line.endsWith(".class"))
                .map(sp -> rootPackage + "/" + sp)
                .collect(Collectors.toSet());
    }

    /**
     * <p>
     * This method takes a list of strings identifying project resources and a string indicating
     * the package being scanned. It returns a set of testable classes within the specified package.
     * Inner classes, enums, records, abstract classes, and classes annotated with ExcludeBeanTesting
     * are excluded from the result set.
     * </p>
     * <p>
     * The method serves as a utility for dynamically retrieving testable classes within a package
     * while excluding non-testable classes like (interface, enum, inner class, record, abstract class
     * and class annotated with {@link ExcludeBeanTesting})
     * </p>
     *
     * @param lines         a list of strings identifying project resources
     * @param packageInScan a string indicating the package being scanned
     * @return a set of testable classes within the specified package
     */
    private static Set<Class<?>> getTestableClassesFromResourcesAsList(final List<String> lines, final String packageInScan) {
        return lines.stream()
                .filter(line -> line.endsWith(".class"))
                .map(line -> getClass(packageInScan, line))
                .filter(clazz -> !clazz.isInterface()
                        && !clazz.getName().contains("$" + clazz.getSimpleName())
                        && !clazz.isEnum()
                        && !Modifier.isAbstract(clazz.getModifiers())
                        && !clazz.isAnnotationPresent(ExcludeBeanTesting.class))
                .collect(Collectors.toSet());
    }

    /**
     * <p>
     * This method takes the name of a class to instantiate and the package name, and returns
     * the corresponding class type. If the class is not found, a ClassNotFoundException is
     * thrown.
     * </p>
     * <p>
     * The method serves as a utility for dynamically retrieving the class type based on its
     * name and package. It is particularly useful for dynamically loading classes at runtime.
     * </p>
     *
     * @param className   the name of the class to instantiate
     * @param packageName the name of the package containing the class
     * @return the corresponding class type
     * @throws IllegalStateException if the class is not found
     */
    private static Class<?> getClass(String packageName, final String className) {
        try {
            return Class.forName(packageName.replace("/", ".") + "."
                    + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Cannot find class: " + className + " in specified package: " + packageName, e);
        }
    }

    /**
     * <p>
     * This method takes the path of a directory to scan and returns a list of strings identifying
     * the resources found within the directory. If the directory does not exist or is empty, an
     * empty list is returned.
     * </p>
     * <p>
     * The method serves as a utility for dynamically scanning directories for project resources.
     * It is particularly useful for retrieving resource paths for further processing within an
     * application.
     * </p>
     *
     * @param directory the path of the directory to scan
     * @return a list of strings identifying the resources found within the directory, or an empty
     * list if the directory does not exist or is empty
     */
    private static List<String> getPackageResourcesAsList(final String directory) {
        return Optional.of(ClassLoader.getSystemClassLoader())
                .map(cl -> cl.getResourceAsStream(directory))
                .map(InputStreamReader::new)
                .map(BufferedReader::new)
                .map(BufferedReader::lines)
                .map(e -> e.collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);
    }

    /**
     * This method is required by the ArgumentsProvider interface and provides
     * arguments for parameterized tests. The provided arguments include the
     * JavaBeans classes to test, automatically retrieved based on the internal
     * behavior of the class.
     *
     * @param context the test execution context
     * @return a stream of Arguments containing arguments for parameterized tests
     */
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        List<Class<?>> classesToTest = new ArrayList<>();

        List<String> packagesToScan = getPackagesToScanFromSourcePath(context);

        packagesToScan.forEach(p -> {
            Deque<String> subPackagesResultToScan = new ArrayDeque<>();

            for (String packageInScan = p; packageInScan != null; packageInScan = subPackagesResultToScan.poll()) {

                List<String> lines = getPackageResourcesAsList(packageInScan);

                classesToTest.addAll(getTestableClassesFromResourcesAsList(lines, packageInScan));

                getSubPackagesFromResourcesAsList(lines, packageInScan)
                        .forEach(subPackagesResultToScan::push);
            }
        });

        log.info(() -> String.format("Found %d testable classes...", (long) classesToTest.size()));

        return classesToTest.stream().map(Arguments::of);
    }

}
