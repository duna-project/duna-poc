/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.util;

import com.google.common.primitives.Primitives;
import com.google.inject.BindingAnnotation;
import io.duna.core.service.Service;

import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by carlos on 19/12/16.
 */
public class Services {

    public static Map<String, String> methodAddressCache =
        new ConcurrentHashMap<>();

    public static Annotation getQualifier(Class<?> serviceClass) {
        return Arrays
            .stream(serviceClass.getAnnotations())
            .filter(a -> !a.annotationType().equals(Service.class) &&
                (a.annotationType().isAnnotationPresent(Qualifier.class) ||
                    a.annotationType().isAnnotationPresent(BindingAnnotation.class))
            )
            .findFirst()
            .orElse(null);
    }

    public static String getInternalServiceAddress(Method method, CharSequence separator) {
        if (methodAddressCache.containsKey(method.toGenericString())) {
            return methodAddressCache.get(method.toGenericString());
        }

        String methodAddress = method.getDeclaringClass().getName() +
            separator +
            method.getName() +
            "(" +
            Arrays.stream(method.getParameterTypes())
                .map(Primitives::wrap)
                .map(pt -> pt.getName().replace("java\\.lang\\.", ""))
                .collect(Collectors.joining(",")) +
            ")";

        return methodAddressCache.put(method.toGenericString(), methodAddress);
    }
}
