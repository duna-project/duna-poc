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
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.implementation.MethodDelegation;

import javax.inject.Inject;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Created by carlos on 19/12/16.
 */
public class ServiceProxyFactory {

    private static final Logger logger = LogManager.getLogManager()
        .getLogger(ServiceProxyFactory.class.getName());

    public static Class<?> loadForContract(Class<?> contractClass) {
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

        // @GeneratedProxy
        AnnotationDescription generatedProxy = AnnotationDescription.Builder
            .ofType(GeneratedProxy.class).build();

        // @formatter:off
        return new ByteBuddy()
            .subclass(Object.class)
            .annotateType(generatedProxy)
            .implement(contractClass)
                .intercept(
                    MethodDelegation
                        .to(new RemoteServiceCallInterceptor())
                        .filter(not(isDeclaredBy(Object.class)))
                )
            .annotateMethod(suspendable)

            // Proxy fields
            .defineField("logger", Logger.class, Visibility.PRIVATE)
                .annotateField(inject)
            .defineField("objectMapper", ObjectMapper.class, Visibility.PRIVATE)
                .annotateField(inject)

            .make()
            .load(ServiceProxyFactory.class.getClassLoader(),
                ClassLoadingStrategy.Default.INJECTION)
            .getLoaded();
        // @formatter:on
    }
}
