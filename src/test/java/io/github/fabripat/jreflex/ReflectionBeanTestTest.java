package io.github.fabripat.jreflex;


import io.github.fabripat.jreflex.annotations.EnableAutomatedBeanTesting;
import io.github.fabripat.jreflex.provider.ClassTestProvider;
import io.github.fabripat.jreflex.testdomain.dto.Dto;
import io.github.fabripat.jreflex.testdomain.dto.DtoWithBuilder;
import io.github.fabripat.jreflex.testdomain.dto.ImmutableDto;
import io.github.fabripat.jreflex.testdomain.entity.EntityWithFieldExcluded;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;


@EnableAutomatedBeanTesting(sourceRootPath = "io.github.fabripat.jreflex.testdomain")
class ReflectionBeanTestTest extends AbstractReflectionBeanTest {

    @Test
    void shouldReturnClassExcludingInnerClassAndEnumAndRecordAndAbstractAndAnnotatedWithExcludeBeanTesting() {

        ClassTestProvider classTestProvider = new ClassTestProvider();

        ExtensionContext extensionContextMock = mock(ExtensionContext.class);
        doReturn(this.getClass()).when(extensionContextMock).getRequiredTestClass();

        List<Object> providedList = classTestProvider.provideArguments(extensionContextMock)
                .map(Arguments::get)
                .flatMap(Stream::of)
                .collect(Collectors.toList());

        List<Object> expectedList = Stream.of(Dto.class, DtoWithBuilder.class, ImmutableDto.class, EntityWithFieldExcluded.class)
                .map(e -> (Object) e)
                .collect(Collectors.toList());

        assertTrue(providedList.containsAll(expectedList));
    }

}