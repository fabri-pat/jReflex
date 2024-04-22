package io.github.fabripat.jreflex;

import com.google.code.beanmatchers.BeanMatchers;
import io.github.fabripat.jreflex.annotations.ExcludeFieldBeanTesting;
import io.github.fabripat.jreflex.provider.ClassTestProvider;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.logging.Logger;

import static com.google.code.beanmatchers.BeanMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * <p>
 * This abstract class serves as a base class that must be extended and annotated with {@link io.github.fabripat.jreflex.annotations.EnableAutomatedBeanTesting}
 * to enable automated testing of JavaBeans.
 * </p>
 * <p>
 * In order to enable automated testing of JavaBeans, subclasses must extend this class and annotate
 * them with the appropriate annotation, such as 'EnableAutomatedBeanTesting'. This annotation signals
 * to the testing framework that the subclass contains JavaBeans to be automatically tested.
 * </p>
 * <p>
 * The class provides a framework for automating the testing of JavaBeans, allowing developers to
 * easily write tests for JavaBeans without manual intervention.
 * </p>
 */
public abstract class AbstractReflectionBeanTest {

    private static final Logger log = Logger.getLogger(AbstractReflectionBeanTest.class.getName());
    private static final UniformRandomProvider RANDOM = RandomSource.XO_RO_SHI_RO_128_PP.create();

    @BeforeAll
    static void setup() {
        log.info(() -> String.format("Setting up value generator for { %s }...", LocalDateTime.class.getName()));
        BeanMatchers.registerValueGenerator(() -> {
            long max = LocalDateTime.MAX.toEpochSecond(ZoneOffset.UTC) - LocalDateTime.MIN.toEpochSecond(ZoneOffset.UTC);
            long randomSeconds = RANDOM.nextLong(0, max);
            return LocalDateTime.MIN.plusSeconds(randomSeconds);
        }, LocalDateTime.class);

        log.info(() -> String.format("Setting up value generator for { %s }...", LocalDate.class.getName()));
        BeanMatchers.registerValueGenerator(() -> {
            long randomDateTime = RANDOM.nextLong(LocalDate.MAX.toEpochDay());
            return LocalDate.ofEpochDay(randomDateTime);
        }, LocalDate.class);
    }

    @ParameterizedTest
    @ArgumentsSource(ClassTestProvider.class)
    void beanTest(Class<?> clazz) {
        String[] excludedFields = Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(ExcludeFieldBeanTesting.class)
                        || Modifier.isFinal(f.getModifiers()))
                .map(Field::getName)
                .toArray(String[]::new);

        String[] finalFields = Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> Modifier.isFinal(f.getModifiers()))
                .map(Field::getName)
                .toArray(String[]::new);

        assertThat(clazz, hasValidBeanConstructor());

        assertThat(clazz, hasValidGettersAndSettersExcluding(finalFields));

        assertThat(clazz, hasValidBeanEqualsExcluding(excludedFields));

        assertThat(clazz, hasValidBeanHashCodeExcluding(excludedFields));

        assertThat(clazz, hasValidBeanToStringExcluding(excludedFields));
    }

}
