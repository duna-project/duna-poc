/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.util;

import io.duna.core.service.Address;
import io.duna.core.service.Service;

import com.google.common.primitives.Primitives;
import com.google.inject.internal.Annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Services {

    private static Map<String, String> methodAddressCache =
        new ConcurrentHashMap<>();

    public static Class<? extends Annotation> getQualifier(Class<?> serviceClass) {
        return Arrays
            .stream(serviceClass.getAnnotations())
            .filter(a -> !(a instanceof Service))
            .map(Annotation::annotationType)
            .filter(Annotations::isBindingAnnotation)
            .findFirst()
            .orElse(null);
    }

    public static String getInternalServiceAddress(Method method) {
        return getInternalServiceAddress(method, ".");
    }

    public static String getInternalServiceAddress(Method method, CharSequence separator) {
        if (methodAddressCache.containsKey(method.toGenericString())) {
            return methodAddressCache.get(method.toGenericString());
        }

        String prefix;
        String methodAddress;

        if (method.getDeclaringClass().isAnnotationPresent(Address.class)) {
            prefix = method.getDeclaringClass().getAnnotation(Address.class).value();
        } else {
            prefix = method.getDeclaringClass().getName();
        }

        if (method.isAnnotationPresent(Address.class)) {
            methodAddress = method.getAnnotation(Address.class).value();
        } else {
            methodAddress = method.getName()
                + "("
                + Arrays.stream(method.getParameterTypes())
                    .map(Primitives::wrap)
                    .map(pt -> pt.getName().replace("java\\.lang\\.", ""))
                    .collect(Collectors.joining(","))
                + ")";
        }

        methodAddressCache.put(method.toGenericString(), prefix + "." + methodAddress);
        return prefix + separator + methodAddress;
    }
}
