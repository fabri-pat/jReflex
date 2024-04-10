package io.github.fabripat.jreflex;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertThrows;


class ReflectionBean extends AbstractReflectionBean {

    @Test
    void shouldThrowClassNotFoundExceptionTryingGetNonExistentClass() {
        Method getClassMethod = getPrivateMethod("getClass", String.class, String.class);

        assertThrows(
                IllegalStateException.class,
                () -> invokeStaticMethod(getClassMethod, "NonExistentClass.class", "package")
        );
    }

    @Test
    void shouldThrowClassNotFoundExceptionTryingGetNonExistentAnnotation() {
        Method getAnnotationClassMethod = getPrivateMethod("getAnnotationClass", String.class);

        assertThrows(
                IllegalStateException.class,
                () -> invokeStaticMethod(getAnnotationClassMethod, "NonExistentAnnotation")
        );
    }

    @Test
    void shouldThrowClassNotFoundExceptionTryingGetAnnotationFindingNotAnnotationClass() {
        Method getAnnotationClassMethod = getPrivateMethod("getAnnotationClass", String.class);

        assertThrows(
                IllegalStateException.class,
                () -> invokeStaticMethod(getAnnotationClassMethod, "it.inps.mstest.entity.notvalidclass.TestInterface")
        );
    }

    private static Method getPrivateMethod(String methodName, Class<?>... classes) {
        try {
            Method declaredMethod = AbstractReflectionBean.class.getDeclaredMethod(methodName, classes);
            declaredMethod.setAccessible(true);

            return declaredMethod;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object invokeStaticMethod(Method method, Object... args) throws Throwable {
        try {
            return method.invoke(null, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
}