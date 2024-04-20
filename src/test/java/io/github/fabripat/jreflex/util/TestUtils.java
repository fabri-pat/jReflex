package io.github.fabripat.jreflex.util;

import io.github.fabripat.jreflex.provider.ClassTestProvider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TestUtils {

    public static Method getPrivateMethod(String methodName, Class<?>... classes) {
        try {
            Method declaredMethod = ClassTestProvider.class.getDeclaredMethod(methodName, classes);
            declaredMethod.setAccessible(true);
            return declaredMethod;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object invokeStaticMethod(Method method, Object... args) throws Throwable {
        try {
            return method.invoke(null, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

}
