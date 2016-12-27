/*
 * Copyright (c) 2016 Duna Open Source Project
 * Ministério do Planejamento, Desenvolvimento de Gestão
 * República Federativa do Brasil
 *
 * This file is part of the Duna Project.
 */
package io.duna.extend;

import com.google.inject.Injector;
import io.vertx.core.Verticle;
import io.vertx.core.spi.VerticleFactory;

import javax.inject.Inject;
import java.lang.reflect.Modifier;
import java.util.logging.Logger;

public class PortVerticleFactory implements VerticleFactory {

    private static final int factoryOrder = 10;

    private final Logger logger;

    private final Injector injector;

    @Inject
    public PortVerticleFactory(Logger logger, Injector injector) {
        this.logger = logger;
        this.injector = injector;
    }

    @Override
    public String prefix() {
        return "duna-port";
    }

    @Override
    public Verticle createVerticle(String verticleName, ClassLoader classLoader) throws Exception {
        Class<?> verticleClass = classLoader.loadClass(VerticleFactory.removePrefix(verticleName));

        if (!Verticle.class.isAssignableFrom(verticleClass)) {
            logger.severe(() -> verticleClass + " isn't a Verticle implementation");
            throw new IllegalArgumentException(verticleClass + " isn't a Verticle implementation.");
        }

        if (verticleClass.isInterface() || Modifier.isAbstract(verticleClass.getModifiers())) {
            logger.severe(() -> verticleClass + " should be a concrete class");
            throw new IllegalArgumentException(verticleClass + " should be a concrete class.");
        }

        logger.fine(() -> "Creating verticle for port " + verticleClass);

        return (Verticle) injector.getBinding(verticleClass).getProvider().get();
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
