/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.proxy;

import co.paralleluniverse.fibers.Suspendable;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;

import javax.enterprise.inject.Default;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.not;

public class ServiceProxyFactory {

    private static final AtomicLong proxyCount = new AtomicLong(0);

    private static final Logger logger = LogManager.getLogManager()
        .getLogger(ServiceProxyFactory.class.getName());

    public static Class<?> loadForContract(Class<?> contractClass) {
        return loadForContract(contractClass, Default.class);
    }

    public static Class<?> loadForContract(Class<?> contractClass, Class<? extends Annotation> qualifier) {
        if (!contractClass.isInterface()) {
            throw new IllegalArgumentException("Contracts must be defined in interfaces.");
        }

        logger.fine(() -> "Creating proxy for " + contractClass);

        // @Inject
        AnnotationDescription inject = AnnotationDescription.Builder
            .ofType(Inject.class).build();

        // @Suspendable
        AnnotationDescription suspendable = AnnotationDescription.Builder
            .ofType(Suspendable.class).build();

        // Qualifier
        AnnotationDescription implQualifier = AnnotationDescription.Builder
            .ofType(qualifier)
            .build();

        // @formatter:off
        return new ByteBuddy()
            .subclass(Object.class)
            .name(contractClass.getName() + "$ServiceProxy$" + proxyCount.incrementAndGet())
            .annotateType(implQualifier)

            // Proxy fields
            .defineField("logger", Logger.class, Visibility.PRIVATE)
                .annotateField(inject)
            .defineField("objectMapper", ObjectMapper.class, Visibility.PRIVATE)
                .annotateField(inject)

            // Implement getter/setter to the qualifier field
            .implement(ServiceProxy.class)
                .intercept(FixedValue
                    .value(qualifier.equals(Default.class) ? "" : qualifier.getName()))

            // Implement the contract class in order to forward the method call
            .implement(contractClass)
                .intercept(
                    MethodDelegation
                        .to(new RemoteServiceCallInterceptor())
                        .filter(not(isDeclaredBy(Object.class)))
                )
                .annotateMethod(suspendable)

            .make()
            .load(ServiceProxyFactory.class.getClassLoader(),
                ClassLoadingStrategy.Default.INJECTION)
            .getLoaded();
        // @formatter:on
    }
}
