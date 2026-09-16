/*
 * Decompiled with CFR 0.152.
 */
package com.fernsehheft.enderchest.nbtapi.wrapper;

import com.fernsehheft.enderchest.nbtapi.NbtApiException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

class DefaultMethodInvoker {
    private static Method invokeDefaultMethod;

    DefaultMethodInvoker() {
    }

    public static Object invokeDefault(Class<?> srcInt, Object target, Method method, Object[] args) {
        if (invokeDefaultMethod != null) {
            try {
                return invokeDefaultMethod.invoke(null, target, method, args);
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new NbtApiException("Error while trying to invoke a default method for Java 9+. " + target + " " + method + " " + Arrays.toString(args), e);
            }
        }
        try {
            Constructor constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class);
            constructor.setAccessible(true);
            return ((MethodHandles.Lookup)constructor.newInstance(srcInt)).in(srcInt).unreflectSpecial(method, srcInt).bindTo(target).invokeWithArguments(args);
        }
        catch (Throwable e) {
            throw new NbtApiException("Error while trying to invoke a default method for Java 8. " + target + " " + method + " " + Arrays.toString(args), e);
        }
    }

    static {
        try {
            invokeDefaultMethod = InvocationHandler.class.getDeclaredMethod("invokeDefault", Object.class, Method.class, Object[].class);
            invokeDefaultMethod.setAccessible(true);
        }
        catch (NoSuchMethodException | SecurityException exception) {
            // empty catch block
        }
    }
}

