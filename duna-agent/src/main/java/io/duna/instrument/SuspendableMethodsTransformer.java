/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.instrument;

import co.paralleluniverse.fibers.Suspendable;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.SuperMethodCall;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Annotates interface methods with {@literal @}{@link Suspendable}.
 *
 * @see co.paralleluniverse.fibers.Suspendable
 * @author <a href="mailto:ceduardo.melo@gmail.com">Carlos Eduardo Melo</a>
 */
public class SuspendableMethodsTransformer implements AgentBuilder.Transformer {

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder,
                                            TypeDescription typeDescription,
                                            ClassLoader classLoader) {
        return builder
            .method(isDeclaredBy(typeDescription).and(not(isDefaultMethod())))
                .withoutCode()
                .annotateMethod(
                    AnnotationDescription.Builder.ofType(Suspendable.class).build())
            .method(isDeclaredBy(typeDescription)
                    .and(isDefaultMethod().or(not(isAbstract()))))
                .intercept(SuperMethodCall.INSTANCE)
                .annotateMethod(
                    AnnotationDescription.Builder.ofType(Suspendable.class).build());
    }
}
