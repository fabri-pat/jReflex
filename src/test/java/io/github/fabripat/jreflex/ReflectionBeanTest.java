package io.github.fabripat.jreflex;


import io.github.fabripat.jreflex.annotations.EnableAutomatedBeanTesting;


@EnableAutomatedBeanTesting(sourceRootPath = "io.github.fabripat.jreflex.testdomain")
class ReflectionBeanTest extends AbstractReflectionBean {

    /*@Test
    void shouldReturnClassExcludingInnerClassAndEnumAndRecordAndAbstractAndAnnotatedWithExcludeBeanTesting() throws Throwable {

        ClassTestProvider classTestProvider = new ClassTestProvider();

        ExtensionContext extensionContextMock = mock(ExtensionContext.class);
        doReturn(this.getClass()).when(extensionContextMock).getRequiredTestClass();

        List<Object> providedList = classTestProvider.provideArguments(extensionContextMock)
                .map(Arguments::get)
                .flatMap(Stream::of)
                .toList();

        List<Object> expectedList = Stream.of(Dto.class, DtoWithBuilder.class, ImmutableDto.class, EntityWithFieldExcluded.class)
                .map(e -> (Object) e)
                .toList();

        assertTrue(providedList.containsAll(expectedList));
    }*/

}