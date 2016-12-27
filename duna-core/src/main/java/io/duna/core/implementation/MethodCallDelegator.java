/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.implementation;

import co.paralleluniverse.fibers.Suspendable;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import java.lang.reflect.Method;

public interface MethodCallDelegator {

    Object invoke(Object ... args);

    static MethodCallDelegator to(Object delegate, Method method) {
        AnnotationDescription suspendable = AnnotationDescription.Builder
            .ofType(Suspendable.class).build();

        try {
            // @formatter:off
            return (MethodCallDelegator) new ByteBuddy()
                .subclass(Object.class)
                .implement(MethodCallDelegator.class)
                    .intercept(
                    MethodCall
                        .invoke(method)
                        .on(delegate)
                        .withArgumentArrayElements(0, method.getParameterCount())
                        .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC)
                    )
                    .annotateMethod(suspendable)
                .make()
                .load(MethodCallDelegator.class.getClassLoader(),
                    ClassLoadingStrategy.Default.INJECTION)
                .getLoaded()
                .newInstance();
            // @formatter:on
        } catch (InstantiationException | IllegalAccessException ex) {
            // TODO fix this
            ex.printStackTrace();
        }

        return null;
    }
}
