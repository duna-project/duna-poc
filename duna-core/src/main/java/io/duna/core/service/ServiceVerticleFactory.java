/*
 * Copyright (c) 2017 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.core.service;

import com.google.inject.BindingAnnotation;
import com.google.inject.Injector;
import com.google.inject.Key;
import io.vertx.core.Verticle;
import io.vertx.core.spi.VerticleFactory;

import javax.inject.Inject;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.util.logging.Logger;

public class ServiceVerticleFactory implements VerticleFactory {

    private static final int factoryOrder = 5;

    private final Logger logger;

    private final Injector injector;

    private final ServiceVerticle.BinderFactory instanceFactory;

    @Inject
    public ServiceVerticleFactory(Logger logger, Injector injector, ServiceVerticle.BinderFactory instanceFactory) {
        this.logger = logger;
        this.injector = injector;
        this.instanceFactory = instanceFactory;
    }

    @Override
    public String prefix() {
        return "duna";
    }

    @Override
    public Verticle createVerticle(String verticleName, ClassLoader classLoader) throws Exception {
        String className;
        String qualifierName = "";

        if (verticleName.contains("@")) {
            qualifierName = VerticleFactory.removePrefix(verticleName).split("@")[0];
            className = VerticleFactory.removePrefix(verticleName).split("@")[1];
        } else {
            className = VerticleFactory.removePrefix(verticleName);
        }

        Class<?> contractClass = classLoader.loadClass(className);

        logger.fine(() -> "Creating verticle for service " + verticleName);

        Object implementation;

        if (qualifierName.isEmpty()) {
            implementation = injector.getBinding(contractClass).getProvider().get();
        } else {
            @SuppressWarnings("unchecked")
            Class<? extends Annotation> qualifierClass =
                (Class<? extends Annotation>) classLoader.loadClass(qualifierName);

            if (qualifierClass.isAnnotation()
                && (qualifierClass.isAnnotationPresent(Qualifier.class)
                    || qualifierClass.isAnnotationPresent(BindingAnnotation.class))) {

                implementation = injector.getBinding(Key.get(contractClass, qualifierClass)).getProvider().get();
            } else {
                throw new IllegalStateException("Services can only be qualified by annotations.");
            }
        }

        return instanceFactory.create(contractClass, implementation);
    }

    @Override
    public int order() {
        return factoryOrder;
    }

    @Override
    public boolean blockingCreate() {
        return true;
    }
}
