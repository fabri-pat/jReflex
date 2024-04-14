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

public class ClassTestProvider implements ArgumentsProvider {

    private static final Logger log = Logger.getLogger(ClassTestProvider.class.getName());

    private static List<String> getPackagesToScanFromSourcePath(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();

        EnableAutomatedBeanTesting enableAutomatedBeanTestingAnnotation =
                getEnableAutomatedBeanTestingAnnotationFromBeanTestClass(testClass);

        String rootPathPackageToScan = enableAutomatedBeanTestingAnnotation.sourceRootPath();

        if (rootPathPackageToScan.isBlank())
            rootPathPackageToScan = testClass.getPackageName();

        String[] packagesToScan = enableAutomatedBeanTestingAnnotation.packagesToScan();

        if (Arrays.asList(packagesToScan).isEmpty())
            return List.of(rootPathPackageToScan);

        String finalRootPathPackageToScan = rootPathPackageToScan;

        return Arrays.stream(packagesToScan)
                .map(p -> finalRootPathPackageToScan.concat(".").concat(p))
                .map(p -> p.replace(".", "/"))
                .toList();
    }

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

    private static Set<String> getSubPackagesFromResourcesAsList(List<String> lines, String rootPackage) {
        return lines.stream()
                .filter(line -> !line.endsWith(".class"))
                .map(sp -> rootPackage + "/" + sp)
                .collect(Collectors.toSet());
    }

    private static Set<Class<?>> getClassesFromResourcesAsList(final List<String> lines, final String packageInScan) {
        return lines.stream()
                .filter(line -> line.endsWith(".class"))
                .map(line -> getClass(packageInScan, line))
                .filter(clazz -> !clazz.isInterface()
                        && !clazz.isEnum()
                        && !clazz.isRecord()
                        && !Modifier.isAbstract(clazz.getModifiers())
                        && !clazz.isAnnotationPresent(ExcludeBeanTesting.class))
                .collect(Collectors.toSet());
    }

    private static Class<?> getClass(String packageName, final String className) {
        try {
            return Class.forName(packageName.replace("/", ".") + "."
                    + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Cannot find class: " + className + " in specified package: " + packageName, e);
        }
    }

    private static List<String> getPackageResourcesAsList(final String directory) {
        return Optional.of(ClassLoader.getSystemClassLoader())
                .map(cl -> cl.getResourceAsStream(directory))
                .map(InputStreamReader::new)
                .map(BufferedReader::new)
                .map(BufferedReader::lines)
                .map(Stream::toList)
                .orElseGet(Collections::emptyList);
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        List<Class<?>> classesToTest = new ArrayList<>();

        List<String> packagesToScan = getPackagesToScanFromSourcePath(context);

        packagesToScan.forEach(p -> {
            Deque<String> subPackagesResultToScan = new ArrayDeque<>();

            for (String packageInScan = p; packageInScan != null; packageInScan = subPackagesResultToScan.poll()) {

                List<String> lines = getPackageResourcesAsList(packageInScan);

                classesToTest.addAll(getClassesFromResourcesAsList(lines, packageInScan));

                getSubPackagesFromResourcesAsList(lines, packageInScan)
                        .forEach(subPackagesResultToScan::push);
            }
        });

        log.info(() -> String.format("Found %d testable classes...", (long) classesToTest.size()));

        return classesToTest.stream().map(Arguments::of);
    }

}
