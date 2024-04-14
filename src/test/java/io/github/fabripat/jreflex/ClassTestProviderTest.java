package io.github.fabripat.jreflex;

import io.github.fabripat.jreflex.provider.ClassTestProvider;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ClassTestProviderTest {
    private static Method getPrivateMethod(String methodName, Class<?>... classes) {
        try {
            Method declaredMethod = ClassTestProvider.class.getDeclaredMethod(methodName, classes);
            declaredMethod.setAccessible(true);
            return declaredMethod;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static void invokeStaticMethod(Method method, Object... args) throws Throwable {
        try {
            method.invoke(null, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    @Test
    void shouldThrowClassNotFoundExceptionTryingGetNonExistentClass() {
        Method getClassMethod = getPrivateMethod("getClass", String.class, String.class);

        assertThrows(
                IllegalStateException.class,
                () -> invokeStaticMethod(getClassMethod, "package", "NonExistentClass.class")
        );
    }
}
