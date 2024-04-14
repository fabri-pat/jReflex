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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.logging.Logger;

import static com.google.code.beanmatchers.BeanMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class AbstractReflectionBean {

    private static final Logger log = Logger.getLogger(AbstractReflectionBean.class.getName());
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

}
