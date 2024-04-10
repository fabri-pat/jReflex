package io.github.fabripat.jreflex;

import com.google.code.beanmatchers.BeanMatchers;
import io.github.fabripat.jreflex.annotations.ExcludeBeanTesting;
import io.github.fabripat.jreflex.annotations.ExcludeFieldBeanTesting;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.code.beanmatchers.BeanMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class AbstractReflectionBean {

    private static final Logger log = Logger.getLogger(AbstractReflectionBean.class.getName());
    private static final Pattern BASE_PACKAGE_PATTERN = Pattern.compile("io\\.github\\.fabripat\\.jreflex");

    private static final UniformRandomProvider RANDOM = RandomSource.XO_RO_SHI_RO_128_PP.create();

    @BeforeAll
    static void setup() {
        /*log.info("Setting up value generator for {}...", LocalDateTime.class.getSimpleName());*/
        BeanMatchers.registerValueGenerator(() -> {
            long max = LocalDateTime.MAX.toEpochSecond(ZoneOffset.UTC) - LocalDateTime.MIN.toEpochSecond(ZoneOffset.UTC);
            long randomSeconds = RANDOM.nextLong(0, max);
            return LocalDateTime.MIN.plusSeconds(randomSeconds);
        }, LocalDateTime.class);

        /*log.info("Setting up value generator for {}...", LocalDate.class.getSimpleName());*/
        BeanMatchers.registerValueGenerator(() -> {
            long randomDateTime = RANDOM.nextLong(LocalDate.MAX.toEpochDay());
            return LocalDate.ofEpochDay(randomDateTime);
        }, LocalDate.class);
    }

    @ParameterizedTest
    @MethodSource(value = "beanToTest")
    void beanTest(Class<?> clazz, int current, int total) {
        /*log.info("{}/{} - Testing bean class {}...", current, total, clazz.getName());*/
        String[] excludedFields = Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(ExcludeFieldBeanTesting.class))
                .map(Field::getName)
                .toArray(String[]::new);
        /*if (excludedFields.length > 0 && log.isInfoEnabled()) {
            log.info("> Excluded fields: {}", Arrays.toString(excludedFields));
        }

        log.info("> Testing has valid bean constructor...");*/
        assertThat(clazz, hasValidBeanConstructor());

        /*log.info("> Testing has valid accessors...");*/
        assertThat(clazz, hasValidGettersAndSetters());

        /*log.info("> Testing has valid equals method...");*/
        assertThat(clazz, hasValidBeanEqualsExcluding(excludedFields));

        /*log.info("> Testing has valid hash code method...");*/
        assertThat(clazz, hasValidBeanHashCodeExcluding(excludedFields));

        /*log.info("> Testing has valid toString method...");*/
        assertThat(clazz, hasValidBeanToStringExcluding(excludedFields));
    }

    static List<Object[]> beanToTest() {
        /*log.info("Searching for classes to test...");*/
        List<Class<?>> classesToTest = new ArrayList<>();

        List<String> rootPackages = getRootPackages();
        /*if (log.isInfoEnabled()) {
            log.info("> Packages to scan: {}", rootPackages.stream()
                    .map(p -> p.replace('/', '.'))
                    .collect(Collectors.joining(", ")));
        }*/

        rootPackages.forEach(p -> {
            Deque<String> subPackagesResultToScan = new ArrayDeque<>();

            for (String packageInScan = p; packageInScan != null; packageInScan = subPackagesResultToScan.pollFirst()) {
                /*log.info("> Analyzing package {}...", packageInScan.replace('/', '.'));*/
                List<String> lines = getPackageResourcesAsList(packageInScan);

                Set<Class<?>> classes = getClassesFromResourcesAsList(lines, packageInScan);
                /*log.info(">> {} classes found", classes.size());*/
                classesToTest.addAll(classes);

                getSubPackagesFromResourcesAsList(lines, packageInScan)
                        .forEach(subPackagesResultToScan::push);
            }
        });

        /*log.info("Scan ended, found {} classes to test", classesToTest.size());*/

        MutableInt counter = new MutableInt();
        return classesToTest.stream()
                .map(c -> new Object[]{c, counter.incrementAndGet(), classesToTest.size()})
                .collect(Collectors.toList());
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
                .map(line -> getClass(line, packageInScan))
                .filter(clazz -> !clazz.isInterface() && !clazz.isEnum() && !Modifier.isAbstract(clazz.getModifiers())
                        && !clazz.isAnnotationPresent(ExcludeBeanTesting.class))
                .collect(Collectors.toSet());
    }

    private static List<String> getPackageResourcesAsList(final String directory) {
        return Optional.of(ClassLoader.getSystemClassLoader())
                .map(cl -> cl.getResourceAsStream(directory))
                .map(InputStreamReader::new)
                .map(BufferedReader::new)
                .map(BufferedReader::lines)
                .map(stream -> stream.collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);
    }

    private static List<String> getRootPackages() {
        String basePackage = Arrays.stream(Package.getPackages())
                .map(p -> BASE_PACKAGE_PATTERN.matcher(p.getName()))
                .filter(Matcher::find)
                .map(Matcher::group)
                .findFirst()
                .orElseThrow(RuntimeException::new)
                .replace(".", "/");

        return Arrays.asList(basePackage + "/dto", basePackage + "/entity");
    }

    private static Class<?> getClass(final String className, String packageName) {
        try {
            return Class.forName(packageName.replace("/", ".") + "."
                    + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Cannot find class: " + className + " in specified package: " + packageName, e);
        }
    }

    private static Class<? extends Annotation> getAnnotationClass(String className) {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends Annotation> annotation = (Class<? extends Annotation>) Class.forName(className);
            if (!annotation.isAnnotation()) throw new IllegalArgumentException(className + " is not an annotation");
            return annotation;
        } catch (ClassNotFoundException cnfe) {
            throw new IllegalStateException("Cannot find annotation " + className, cnfe);
        }
    }

}
