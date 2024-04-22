package io.github.fabripat.jreflex;

import io.github.fabripat.jreflex.annotations.EnableAutomatedBeanTesting;
import io.github.fabripat.jreflex.testdomain.dto.Dto;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.PreconditionViolationException;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static io.github.fabripat.jreflex.util.TestUtils.getPrivateMethod;
import static io.github.fabripat.jreflex.util.TestUtils.invokeStaticMethod;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ClassTestProviderTest {

    @Nested
    class getClassMethod {
        @Test
        void shouldThrowClassNotFoundExceptionTryingGetNonExistentClass() {
            Method getClassMethod = getPrivateMethod("getClass", String.class, String.class);

            assertThrows(
                    IllegalStateException.class,
                    () -> invokeStaticMethod(getClassMethod, "package", "NonExistentClass.class")
            );
        }

        @Test
        void shouldReturnExpectedClassTryingGeExistentClass() throws Throwable {
            Method getClassMethod = getPrivateMethod("getClass", String.class, String.class);

            @SuppressWarnings("unchecked")
            Class<Dto> result = (Class<Dto>) invokeStaticMethod(getClassMethod, Dto.class.getPackageName(), Dto.class.getSimpleName() + ".class");

            assertEquals(Dto.class, result);
        }

    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class getPathsFromEnableAutomatedBeanTestingAnnotationMethod {
        private Stream<Arguments> provideEnableAutomatedBeanTestingAnnotations() {
            return Stream.of(
                    Arguments.of(getInstanceOfEnableAutomatedBeanTestingAnnotation("com.my.test", new String[]{})),
                    Arguments.of(getInstanceOfEnableAutomatedBeanTestingAnnotation("com.my.test", new String[]{"dto"})),
                    Arguments.of(getInstanceOfEnableAutomatedBeanTestingAnnotation("com.my.test", new String[]{"dto", "domain"}))
            );
        }

        private EnableAutomatedBeanTesting getInstanceOfEnableAutomatedBeanTestingAnnotation(String packageRoot, String[] subPackages) {
            return new EnableAutomatedBeanTesting() {

                @Override
                public Class<? extends Annotation> annotationType() {
                    return this.getClass();
                }

                @Override
                public String sourceRootPath() {
                    return packageRoot;
                }

                @Override
                public String[] packagesToScan() {
                    return subPackages;
                }
            };
        }

        @ParameterizedTest
        @MethodSource("provideEnableAutomatedBeanTestingAnnotations")
        void shouldReturnComposedPathListFromAnnotation(EnableAutomatedBeanTesting testAnnotation) throws Throwable {
            Method getPathsFromEnableAutomatedBeanTestingAnnotation = getPrivateMethod("getPathsFromEnableAutomatedBeanTestingAnnotation",
                    EnableAutomatedBeanTesting.class
            );

            @SuppressWarnings("unchecked")
            List<String> result = (List<String>) invokeStaticMethod(getPathsFromEnableAutomatedBeanTestingAnnotation, testAnnotation);

            Arrays.stream(testAnnotation.packagesToScan()).map(e -> testAnnotation.sourceRootPath().replace(".", "/").concat("/" + e)).forEach(e -> assertTrue(result.contains(e)));
        }

    }

    @Nested
    class getEnableAutomatedBeanTestingAnnotationFromBeanTestClassMethod {

        @Test
        void shouldThrowPreconditionViolationExceptionTryingGettingEnableAutomatedBeanTestingAnnotationFromClassNotAnnotatedWith() {
            Method getEnableAutomatedBeanTestingAnnotationFromBeanTestClassMethod = getPrivateMethod(
                    "getEnableAutomatedBeanTestingAnnotationFromBeanTestClass",
                    Class.class
            );

            Throwable exception = assertThrows(
                    PreconditionViolationException.class,
                    () -> invokeStaticMethod(getEnableAutomatedBeanTestingAnnotationFromBeanTestClassMethod, Object.class)
            );

            assertEquals(
                    exception.getMessage(),
                    "{ " + Object.class.getName() + " }"
                            + " class must be annotated with { " + EnableAutomatedBeanTesting.class.getName()
                            + " } annotation. Please provide it or disable the test."
            );
        }

        @Test
        void shouldReturnAnnotationEnableAutomatedBeanTestingAnnotationFromClassAnnotatedWith() throws Throwable {
            Method getEnableAutomatedBeanTestingAnnotationFromBeanTestClassMethod = getPrivateMethod(
                    "getEnableAutomatedBeanTestingAnnotationFromBeanTestClass",
                    Class.class
            );

            Annotation result = (Annotation) invokeStaticMethod(getEnableAutomatedBeanTestingAnnotationFromBeanTestClassMethod, ReflectionBeanTestTest.class);

            assertEquals(
                    EnableAutomatedBeanTesting.class,
                    result.annotationType()
            );
        }

    }

    @Nested
    class getSubPackagesFromResourcesAsListMethod {
        @Test
        void shouldReturnRootPackageConcatenatedWithEveryLinesNotEndsWithClass() throws Throwable {
            Method getSubPackagesFromResourcesAsList = getPrivateMethod("getSubPackagesFromResourcesAsList",
                    List.class, String.class
            );

            List<String> lines = List.of(
                    "element1.class",
                    "element2",
                    "element3",
                    "element3",
                    "element4.class"
            );

            @SuppressWarnings("unchecked")
            Set<String> result = (Set<String>) invokeStaticMethod(getSubPackagesFromResourcesAsList, lines, "com.mydomain.myproject");

            assertEquals(result, Set.of("com.mydomain.myproject/element2", "com.mydomain.myproject/element3"));
        }

        @Test
        void shouldReturnEmptySet() throws Throwable {
            Method getSubPackagesFromResourcesAsList = getPrivateMethod("getSubPackagesFromResourcesAsList",
                    List.class, String.class
            );

            List<String> lines = List.of(
                    "element1.class",
                    "element2.class",
                    "element3.class",
                    "element3.class",
                    "element4.class"
            );

            @SuppressWarnings("unchecked")
            Set<String> result = (Set<String>) invokeStaticMethod(getSubPackagesFromResourcesAsList, lines, "com.mydomain.myproject");

            assertEquals(Collections.EMPTY_SET, result);
        }

    }

}
